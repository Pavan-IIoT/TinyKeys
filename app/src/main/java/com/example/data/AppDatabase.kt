package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "song_scores")
data class SongScore(
    @PrimaryKey val songId: String,
    val stars: Int
)

@Dao
interface SongScoreDao {
    @Query("SELECT * FROM song_scores")
    fun getAllScores(): Flow<List<SongScore>>

    @Query("SELECT * FROM song_scores WHERE songId = :songId")
    suspend fun getScore(songId: String): SongScore?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: SongScore)
}

@Database(entities = [SongScore::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songScoreDao(): SongScoreDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tinykeys_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
