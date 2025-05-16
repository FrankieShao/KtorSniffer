package io.github.frankieshao.ktorsniffer.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import io.github.frankieshao.ktorsniffer.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.jvm.JvmStatic

/**
 * @author Frank
 * @created 5/8/25
 * Data class representing a single network transaction log.
 * Fields are designed to be populated in two stages: initial request and subsequent response.
 */
@Serializable
@Entity
data class NetworkLog(
    // --- Core Identification & Request Phase ---
    @PrimaryKey val id: String, // Primary Key, unique identifier for the log entry
    val requestUrl: String,
    val requestMethod: String,
    val requestHeaders: Map<String, List<String>>,
    val requestBodyType: String, // ContentType string of the request, e.g., "application/json"
    val requestBodyLength: Long, // Reported Content-Length of the request body
    var requestBody: String? = null, // Captured request body string (based on strategy)

    // --- Response Phase (populated when response is received) ---
    var requestTimestamp: Long? = null, // Start time of the request (from Ktor's requestTime)
    var responseTimestamp: Long? = null, // Time when the response was received (from Ktor's responseTime)

    var responseStatusCode: Int? = null,
    var responseHeaders: Map<String, List<String>>? = null,
    var responseBodyType: String? = null, // ContentType string of the response
    var responseBodyLength: Long? = null, // Reported Content-Length of the response body
    var responseBody: String? = null, // Captured response body string (based on strategy)

    // --- General HTTP & Meta Information ---
    var protocol: String? = null, // e.g., "HTTP/1.1", "HTTP/2.0" (from Ktor's HttpResponse)
    var errorDetails: String? = null // For logging client-side errors (e.g., network issues) or server-side error messages
)

object MapListStringConverter {

    private val mapSerializer = MapSerializer(String.serializer(), ListSerializer(String.serializer()))

    @TypeConverter
    @JvmStatic
    fun fromStringMap(value: String?): Map<String, List<String>> {
        return value?.let {
            try {
                Json.decodeFromString(mapSerializer, it)
            } catch (e: Exception) {
                emptyMap()
            }
        } ?: emptyMap()
    }

    @TypeConverter
    @JvmStatic
    fun toStringMap(map: Map<String, List<String>>?): String? {
        return map?.let {
            try {
                Json.encodeToString(mapSerializer, it)
            } catch (e: Exception) {
                Logger.e("encode to json error: ${e.message}")
                null
            }
        }
    }
}