package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.sin

object SoundManager {
    private var scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private const val SAMPLE_RATE = 44100
    private const val DEFAULT_DURATION_MS = 600

    private const val POOL_SIZE = 8
    private val trackPool = ArrayDeque<AudioTrack>()
    private val poolLock = Any()
    private val MAX_TRACK_BUFFER_BYTES = (2000 * SAMPLE_RATE / 1000) * 2

    private fun createTrack(bufferSize: Int): AudioTrack {
        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
    }

    private val frequencies = mapOf(
        "C4" to 261.63, "C#4" to 277.18, "D4" to 293.66, "D#4" to 311.13,
        "E4" to 329.63, "F4" to 349.23, "F#4" to 369.99, "G4" to 392.00,
        "G#4" to 415.30, "A4" to 440.00, "A#4" to 466.16, "B4" to 493.88,
        "C5" to 523.25, "C#5" to 554.37, "D5" to 587.33, "D#5" to 622.25,
        "E5" to 659.25, "F5" to 698.46, "F#5" to 739.99, "G5" to 783.99,
        "G#5" to 830.61, "A5" to 880.00, "A#5" to 932.33, "B5" to 987.77
    )

    private val toneCache = java.util.concurrent.ConcurrentHashMap<String, ShortArray>()
    private lateinit var correctFeedbackBytes: ShortArray
    private lateinit var wrongFeedbackBytes: ShortArray

    fun init() {
        if (!scope.isActive) scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            synchronized(poolLock) {
                while (trackPool.size < POOL_SIZE) {
                    try {
                        trackPool.addLast(createTrack(MAX_TRACK_BUFFER_BYTES))
                    } catch (e: Exception) {
                        break
                    }
                }
            }
            listOf(300, 600, 1200).forEach { duration ->
                frequencies.forEach { (note, freq) ->
                    toneCache["$note-$duration"] = generateTone(freq, duration)
                }
            }
            correctFeedbackBytes = generateTone(880.0, 150)
            wrongFeedbackBytes = generateTone(150.0, 300)
        }
    }

    fun release() {
        scope.cancel()
        toneCache.clear()
        synchronized(poolLock) {
            trackPool.forEach { try { it.release() } catch (e: Exception) {} }
            trackPool.clear()
        }
    }

    private fun generateTone(freq: Double, durationMs: Int): ShortArray {
        val numSamples = (durationMs * SAMPLE_RATE) / 1000
        val sample = ShortArray(numSamples)
        val envelopeAttack = (0.02 * SAMPLE_RATE).toInt() // 20ms attack
        val envelopeRelease = (numSamples * 0.3).toInt() // 30% of note duration

        for (i in 0 until numSamples) {
            val angle = 2.0 * Math.PI * i / (SAMPLE_RATE / freq)
            val envelope = when {
                i < envelopeAttack -> i.toDouble() / envelopeAttack
                i > numSamples - envelopeRelease -> (numSamples - i).toDouble() / envelopeRelease
                else -> 1.0
            }
            sample[i] = (sin(angle) * Short.MAX_VALUE * envelope * 0.7).toInt().toShort()
        }
        return sample
    }

    fun playNote(note: String, durationMs: Int = DEFAULT_DURATION_MS) {
        val key = "$note-$durationMs"
        val bytes = toneCache[key]
        if (bytes != null) {
            playBuffer(bytes, durationMs)
        } else {
            scope.launch {
                val freq = frequencies[note] ?: return@launch
                val generated = generateTone(freq, durationMs)
                toneCache[key] = generated
                playBuffer(generated, durationMs)
            }
        }
    }

    fun playCorrectFeedback() {
        if (::correctFeedbackBytes.isInitialized) playBuffer(correctFeedbackBytes, 150)
    }

    fun playWrongFeedback() {
        if (::wrongFeedbackBytes.isInitialized) playBuffer(wrongFeedbackBytes, 300)
    }

    private val activeTracks = java.util.concurrent.atomic.AtomicInteger(0)
    private const val MAX_TRACKS = 16

    private fun playBuffer(bytes: ShortArray, durationMs: Int) {
        if (activeTracks.incrementAndGet() > MAX_TRACKS) {
            activeTracks.decrementAndGet()
            return
        }

        scope.launch {
            var track: AudioTrack? = null
            try {
                val requiredBufferSize = bytes.size * 2
                track = synchronized(poolLock) {
                    trackPool.removeFirstOrNull()
                } ?: createTrack(Math.max(requiredBufferSize, MAX_TRACK_BUFFER_BYTES))

                val paddedBytes = if (bytes.size * 2 < MAX_TRACK_BUFFER_BYTES) {
                    val arr = ShortArray(MAX_TRACK_BUFFER_BYTES / 2)
                    System.arraycopy(bytes, 0, arr, 0, bytes.size)
                    arr
                } else {
                    bytes
                }

                track.write(paddedBytes, 0, paddedBytes.size)
                track.play()
                
                delay(durationMs.toLong())
                try {
                    track.stop()
                } catch (e: Exception) {}
            } catch (e: Exception) {
                // Ignore audio track exhaustion/limit errors to prevent application crash
            } finally {
                if (track != null) {
                    try {
                        track.stop()
                        track.reloadStaticData()
                        track.setPlaybackHeadPosition(0)
                        val added = synchronized(poolLock) {
                            if (trackPool.size < POOL_SIZE) {
                                trackPool.addLast(track)
                                true
                            } else false
                        }
                        if (!added) {
                            track.release()
                        }
                    } catch (e: Exception) {
                        try { track.release() } catch (ignored: Exception) {}
                    }
                }
                activeTracks.decrementAndGet()
            }
        }
    }
}
