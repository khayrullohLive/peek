package io.github.peek.core

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Single HTTP log entry.
 *
 * Only [endpoint] and [date] are required — everything else is nullable.
 * The interceptor fills in what it can; in exotic cases (e.g. request
 * failed before reaching the network) most fields stay null but the
 * log is still valid with just endpoint + date.
 */
@Entity(tableName = "peek_logs")
data class PeekLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // ─── Required ─────────────────────────────────────────────────────────
    /** Full URL including query string */
    val endpoint: String,
    /** Epoch millis when the request started */
    val date: Long,

    // ─── Nullable ─────────────────────────────────────────────────────────
    val method: String?          = null,
    val host: String?            = null,
    val path: String?            = null,
    val durationMs: Long?        = null,
    val statusCode: Int?         = null,
    val requestHeaders: String?  = null,
    val requestBody: String?     = null,
    val responseHeaders: String? = null,
    val responseBody: String?    = null,
    val error: String?           = null,
) {
    val isSuccess: Boolean get() = statusCode in 200..299
    val isError: Boolean   get() = (statusCode != null && statusCode >= 400) || error != null
    val isPending: Boolean get() = statusCode == null && error == null
}

@Dao
interface PeekLogDao {
    @Query("SELECT * FROM peek_logs ORDER BY date DESC")
    fun observeAll(): Flow<List<PeekLog>>

    @Query("SELECT * FROM peek_logs ORDER BY date DESC")
    suspend fun getAll(): List<PeekLog>

    @Query("SELECT * FROM peek_logs WHERE id = :id")
    suspend fun findById(id: Long): PeekLog?

    @Insert
    suspend fun insert(log: PeekLog): Long

    @Update
    suspend fun update(log: PeekLog)

    @Query("DELETE FROM peek_logs")
    suspend fun clear()
}

@Database(entities = [PeekLog::class], version = 1, exportSchema = false)
abstract class PeekDatabase : RoomDatabase() {
    abstract fun logDao(): PeekLogDao
}
