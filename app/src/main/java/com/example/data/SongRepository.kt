package com.example.data

object SongRepository {
    data class SongNote(val note: String, val durationBeats: Float)

    data class Song(
        val id: String,
        val name: String,
        val bpm: Int,
        val notes: List<SongNote>,
        val difficultyStars: Int
    )

    val songs = listOf(
        Song(
            id = "twinkle",
            name = "Twinkle Twinkle Little Star",
            bpm = 100,
            difficultyStars = 1,
            notes = listOf("C4","C4","G4","G4","A4","A4","G4","F4","F4","E4","E4","D4","D4","C4",
                "G4","G4","F4","F4","E4","E4","D4","G4","G4","F4","F4","E4","E4","D4",
                "C4","C4","G4","G4","A4","A4","G4","F4","F4","E4","E4","D4","D4","C4")
                .zip(listOf(
                    1f, 1f, 1f, 1f, 1f, 1f, 2f,  1f, 1f, 1f, 1f, 1f, 1f, 2f,
                    1f, 1f, 1f, 1f, 1f, 1f, 2f,  1f, 1f, 1f, 1f, 1f, 1f, 2f,
                    1f, 1f, 1f, 1f, 1f, 1f, 2f,  1f, 1f, 1f, 1f, 1f, 1f, 2f
                )) { n, d -> SongNote(n, d) }
        ),
        Song(
            id = "happy_birthday",
            name = "Happy Birthday",
            bpm = 120,
            difficultyStars = 2,
            notes = listOf("C4","C4","D4","C4","F4","E4","C4","C4","D4","C4","G4","F4",
                "C4","C4","C5","A4","F4","E4","D4","A#4","A#4","A4","F4","G4","F4")
                .zip(listOf(
                    0.75f, 0.25f, 1f, 1f, 1f, 2f,
                    0.75f, 0.25f, 1f, 1f, 1f, 2f,
                    0.75f, 0.25f, 1f, 1f, 1f, 1f, 1f,
                    0.75f, 0.25f, 1f, 1f, 1f, 2f
                )) { n, d -> SongNote(n, d) }
        ),
        Song(
            id = "mary",
            name = "Mary Had a Little Lamb",
            bpm = 110,
            difficultyStars = 1,
            notes = listOf("E4","D4","C4","D4","E4","E4","E4","D4","D4","D4","E4","G4","G4",
                "E4","D4","C4","D4","E4","E4","E4","E4","D4","D4","E4","D4","C4")
                .zip(listOf(
                    1f, 1f, 1f, 1f, 1f, 1f, 2f,  1f, 1f, 2f,  1f, 1f, 2f,
                    1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f,  1f, 1f, 1f, 1f, 4f
                )) { n, d -> SongNote(n, d) }
        ),
        Song(
            id = "baa_baa",
            name = "Baa Baa Black Sheep",
            bpm = 100,
            difficultyStars = 2,
            notes = listOf("C4","C4","G4","G4","A4","A4","A4","A4","G4","F4","F4","E4","E4","D4","D4","C4")
                .zip(listOf(
                    1f, 1f, 1f, 1f,
                    0.5f, 0.5f, 0.5f, 0.5f, 2f,
                    1f, 1f, 1f, 1f,
                    1f, 1f, 2f
                )) { n, d -> SongNote(n, d) }
        ),
        Song(
            id = "jingle",
            name = "Jingle Bells",
            bpm = 160,
            difficultyStars = 3,
            notes = listOf("E4","E4","E4","E4","E4","E4","E4","G4","C4","D4","E4",
                "F4","F4","F4","F4","F4","E4","E4","E4","E4","E4","D4","D4","E4","D4","G4")
                .zip(listOf(
                    1f, 1f, 2f,  1f, 1f, 2f,  1f, 1f, 1.5f, 0.5f, 4f,
                    1f, 1f, 1f, 1f,  1f, 1f, 1f, 0.5f, 0.5f,  1f, 1f, 1f, 1f,  2f, 2f
                )) { n, d -> SongNote(n, d) }
        )
    )

    fun getSong(id: String) = songs.find { it.id == id }
}
