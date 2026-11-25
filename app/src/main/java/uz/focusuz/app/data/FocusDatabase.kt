package uz.focusuz.app.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tasks")
data class FocusTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val completed: Boolean = false,
    val priority: Int = 0,
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val dueAt: Long? = null,
    val archived: Boolean = false
)

@Entity(tableName = "notes")
data class FocusNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val color: Int = 0,
    val pinned: Boolean = false,
    val archived: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val durationSeconds: Long,
    val type: String = "focus",
    val finishedAt: Long = System.currentTimeMillis()
)

@Dao
interface FocusDao {

    @Query("SELECT * FROM tasks ORDER BY priority DESC, createdAt DESC")
    fun tasks(): Flow<List<FocusTask>>

    @Insert
    suspend fun addTask(task: FocusTask): Long

    @Update
    suspend fun updateTask(task: FocusTask)

    @Delete
    suspend fun deleteTask(task: FocusTask)

    @Query("SELECT * FROM notes ORDER BY pinned DESC, updatedAt DESC")
    fun notes(): Flow<List<FocusNote>>

    @Insert
    suspend fun addNote(note: FocusNote): Long

    @Update
    suspend fun updateNote(note: FocusNote)

    @Delete
    suspend fun deleteNote(note: FocusNote)

    @Query("SELECT * FROM sessions ORDER BY finishedAt DESC")
    fun sessions(): Flow<List<FocusSession>>

    @Insert
    suspend fun addSession(session: FocusSession): Long
}

@Database(
    entities = [
        FocusTask::class,
        FocusNote::class,
        FocusSession::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FocusDatabase : RoomDatabase() {

    abstract fun dao(): FocusDao

    companion object {
        @Volatile
        private var instance: FocusDatabase? = null

        fun get(context: Context): FocusDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FocusDatabase::class.java,
                    "focusuz.db"
                ).build().also { instance = it }
            }
        }
    }
}
