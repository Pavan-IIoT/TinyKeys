package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.sin

object SoundManager {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private const val SAMPLE_RATE = 44100
    private const val DURATION_MS = 1500

    private val frequencies = mapOf(
        "C4" to 261.63, "C#4" to 277.18, "D4" to 293.66, "D#4" to 311.13,
        "E4" to 329.63, "F4" to 349.23, "F#4" to 369.99, "G4" to 392.00,
        "G#4" to 415.30, "A4" to 440.00, "A#4" to 466.16, "B4" to 493.88,
        "C5" to 523.25, "C#5" to 554.37, "D5" to 587.33, "D#5" to 622.25,
        "E5" to 659.25, "F5" to 698.46, "F#5" to 739.99, "G5" to 783.99,
        "G#5" to 830.61, "A5" to 880.00, "A#5" to 932.33, "B5" to 987.77
    )

    private val noteBytes = mutableMapOf<String, ShortArray>()
    private lateinit var correctFeedbackBytes: ShortArray
    private lateinit var wrongFeedbackBytes: ShortArray

    fun init() {
        scope.launch {
            frequencies.forEach { (note, freq) ->
                noteBytes[note] = generateTone(freq, DURATION_MS)
            }
            correctFeedbackBytes = generateTone(880.0, 150)
            wrongFeedbackBytes = generateTone(150.0, 300)
        }
    }

    private fun generateTone(freq: Double, durationMs: Int): ShortArray {
        val numSamples = (durationMs * SAMPLE_RATE) / 1000
        val sample = ShortArray(numSamples)
        val envelopeAttack = (0.05 * SAMPLE_RATE).toInt() // 50ms attack
        val envelopeRelease = (1.0 * SAMPLE_RATE).toInt() // 1s release

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

    fun playNote(note: String) {
        val bytes = noteBytes[note] ?: return
        playBuffer(bytes, DURATION_MS)
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
                track = AudioTrack.Builder()
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
                    .setBufferSizeInBytes(bytes.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(bytes, 0, bytes.size)
                track.play()
                
                // Release after playing
                delay(durationMs.toLong() + 100)
            } catch (e: Exception) {
                // Ignore audio track exhaustion/limit errors to prevent application crash
            } finally {
                try {
                    track?.release()
                } catch (e: Exception) {
                    // Ignore release errors
                }
                activeTracks.decrementAndGet()
            }
        }
    }
}
