package com.abdullah.life.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries WHERE isDeleted = 0 AND isDraft = 0 ORDER BY createdAt DESC")
    fun getAllActiveEntries(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE isFavorite = 1 AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getFavoriteEntries(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE isDeleted = 1 ORDER BY deletedTimestamp DESC")
    fun getDeletedEntries(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE isDraft = 1 LIMIT 1")
    suspend fun getActiveDraft(): DiaryEntry?

    @Query("SELECT * FROM diary_entries WHERE id = :entryId LIMIT 1")
    suspend fun getEntryById(entryId: Long): DiaryEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: DiaryEntry): Long

    @Query("DELETE FROM diary_entries WHERE isDeleted = 1 AND deletedTimestamp < :thresholdTime")
    suspend fun purgeOldDeletedEntries(thresholdTime: Long)

    @Query("SELECT * FROM diary_entries WHERE isDeleted = 0 AND (title LIKE '%' || :query || '%' OR contentBlocksJson LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    fun searchEntries(query: String): Flow<List<DiaryEntry>>
}

@Database(entities = [DiaryEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "abdullah_life.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}