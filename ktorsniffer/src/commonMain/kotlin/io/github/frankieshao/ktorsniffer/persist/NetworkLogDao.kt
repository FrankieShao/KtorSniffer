package io.github.frankieshao.ktorsniffer.persist

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.frankieshao.ktorsniffer.model.NetworkLog
import io.github.frankieshao.ktorsniffer.model.NetworkLogSummary
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing network log entries in the database.
 * Provides methods for inserting, querying, and deleting logs.
 */
@Dao
interface NetworkLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: NetworkLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(logs: List<NetworkLog>)

    @Query("""
        SELECT id, requestUrl, requestMethod, responseStatusCode, responseBodyType, requestTimestamp FROM NetworkLog
        WHERE (:query = '' OR requestUrl LIKE '%' || :query || '%' OR requestMethod LIKE '%' || :query || '%' OR responseStatusCode LIKE '%' || :query || '%')
        ORDER BY requestTimestamp DESC
        LIMIT :limit 
    """)
    fun getAllAsFlow(query: String, limit: Int): Flow<List<NetworkLogSummary>>

    @Query("""
        SELECT * FROM NetworkLog
        WHERE (:query = '' OR requestUrl LIKE '%' || :query || '%' OR requestMethod LIKE '%' || :query || '%' OR responseStatusCode LIKE '%' || :query || '%')
        ORDER BY requestTimestamp DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getPagedLog(query: String, limit: Int, offset: Int): List<NetworkLog>

    @Query("SELECT * FROM NetworkLog WHERE id = :id")
    suspend fun getById(id: String): NetworkLog?

    @Query("SELECT COUNT(*) as count FROM NetworkLog")
    suspend fun count(): Int

    @Query("DELETE FROM NetworkLog")
    suspend fun clearAllLogs()

    @Delete
    suspend fun delete(log: NetworkLog)
}