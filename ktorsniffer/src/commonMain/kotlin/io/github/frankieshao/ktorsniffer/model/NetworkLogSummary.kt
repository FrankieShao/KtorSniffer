package io.github.frankieshao.ktorsniffer.model

/**
 * Summary data class for a network log entry.
 * Used for list displays.
 */
data class NetworkLogSummary(
    val id: String,
    val requestUrl: String,
    val requestMethod: String,
    val responseStatusCode: Int?,
    val responseBodyType: String?,
    val requestTimestamp: Long?
)