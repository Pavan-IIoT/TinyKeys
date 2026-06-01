package com.example.data

object SongRepository {
    data class Song(
        val id: String,
        val name: String,
        val notes: List<String>,
        val difficultyStars: Int
    )

    val songs = listOf(
        Song(
            id = "twinkle",
            name = "Twinkle Twinkle Little Star",
            difficultyStars = 1,
            notes = listOf("C4","C4","G4","G4","A4","A4","G4","F4","F4","E4","E4","D4","D4","C4",
                "G4","G4","F4","F4","E4","E4","D4","G4","G4","F4","F4","E4","E4","D4",
                "C4","C4","G4","G4","A4","A4","G4","F4","F4","E4","E4","D4","D4","C4")
        ),
        Song(
            id = "happy_birthday",
            name = "Happy Birthday",
            difficultyStars = 2,
            notes = listOf("C4","C4","D4","C4","F4","E4","C4","C4","D4","C4","G4","F4",
                "C4","C4","C5","A4","F4","E4","D4","A#4","A#4","A4","F4","G4","F4")
        ),
        Song(
            id = "mary",
            name = "Mary Had a Little Lamb",
            difficultyStars = 1,
            notes = listOf("E4","D4","C4","D4","E4","E4","E4","D4","D4","D4","E4","G4","G4",
                "E4","D4","C4","D4","E4","E4","E4","E4","D4","D4","E4","D4","C4")
        ),
        Song(
            id = "baa_baa",
            name = "Baa Baa Black Sheep",
            difficultyStars = 2,
            notes = listOf("C4","C4","G4","G4","A4","A4","A4","A4","G4","F4","F4","E4","E4","D4","D4","C4")
        ),
        Song(
            id = "jingle",
            name = "Jingle Bells",
            difficultyStars = 3,
            notes = listOf("E4","E4","E4","E4","E4","E4","E4","G4","C4","D4","E4",
                "F4","F4","F4","F4","F4","E4","E4","E4","E4","E4","D4","D4","E4","D4","G4")
        )
    )

    fun getSong(id: String) = songs.find { it.id == id }
}
