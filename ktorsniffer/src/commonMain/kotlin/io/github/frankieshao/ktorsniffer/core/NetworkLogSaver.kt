package io.github.frankieshao.ktorsniffer.core

import io.github.frankieshao.ktorsniffer.KtorSniffer
import io.github.frankieshao.ktorsniffer.Logger
import io.github.frankieshao.ktorsniffer.model.NetworkLog
import io.github.frankieshao.ktorsniffer.persist.NetworkLogDao
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job

/**
 * @author Frank
 * @created 5/14/25
 */

/**
 * Creates a new instance of NetworkLogSaver using the current NetworkLogDao from KtorSniffer.
 */
internal fun newSaver(): NetworkLogSaver = NetworkLogSaver(KtorSniffer.getNetworkLogDao())

/**
 * Responsible for saving network request and response logs.
 * Handles synchronization and ensures that logs are only saved once per request/response pair.
 */
internal class NetworkLogSaver(
    private val networkLogDao: NetworkLogDao
) {

    private lateinit var request: RequestSaveEntry
    private lateinit var response: ResponseSaveEntry
    private var error: String? = null

    // Jobs to monitor when request and response are saved
    private val requestSavedMonitor = Job()
    private val responseSavedMonitor = Job()
    // Atomic flags to ensure each save operation only happens once
    private val requestSaved = atomic(false)
    private val responseSaved = atomic(false)
    private val finished = atomic(false)

    /**
     * Saves the request entry. Only the first call will have an effect.
     */
    fun saveRequest(request: RequestSaveEntry) {
        if (!requestSaved.compareAndSet(false, true)) return
        this.request = request
        requestSavedMonitor.complete()
        Logger.d("save Request success")
    }

    /**
     * Records an exception that occurred during the response phase.
     */
    fun responseException(error: String) {
        this.error = error
        Logger.d("save Response Exception")
    }

    /**
     * Saves the response entry. Only the first call will have an effect.
     */
    fun saveResponse(response: ResponseSaveEntry) {
        if (!responseSaved.compareAndSet(false, true)) return
        this.response = response
        responseSavedMonitor.complete()
        Logger.d("save Response success")
    }

    /**
     * Finalizes the log entry and inserts it into the database.
     * Waits for both request and response to be saved before inserting.
     */
    suspend fun finish(id: String) {
        if (!finished.compareAndSet(false, true)) return
        requestSavedMonitor.join()
        responseSavedMonitor.join()
        networkLogDao.insert(
            NetworkLog(
                id = id,
                requestUrl = request.url,
                requestMethod = request.method,
                requestHeaders = request.headers,
                requestBodyType = request.bodyType,
                requestBodyLength = request.bodyLength,
                requestBody = request.body,
                requestTimestamp = response.requestTime,
                responseTimestamp = response.responseTime,
                responseStatusCode = response.statusCode,
                responseHeaders = response.headers,
                responseBodyType = response.bodyType,
                responseBodyLength = response.bodyLength,
                responseBody = response.body,
                protocol = response.protocol,
                errorDetails = error,
            )
        )
        Logger.d("insert to database success")
    }
}

/**
 * Data class representing the information to be saved for a network request.
 */
internal data class RequestSaveEntry(
    val url: String,
    val method: String,
    val headers: Map<String, List<String>>,
    val bodyType: String,
    val body: String?,
    val bodyLength: Long
)

/**
 * Data class representing the information to be saved for a network response.
 */
internal data class ResponseSaveEntry(
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val bodyType: String,
    val body: String?,
    val bodyLength: Long,
    val protocol: String,
    val requestTime: Long,
    val responseTime: Long
)