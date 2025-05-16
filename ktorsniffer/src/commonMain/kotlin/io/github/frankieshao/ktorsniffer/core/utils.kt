package io.github.frankieshao.ktorsniffer.core

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.util.toMap
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.readText
import io.ktor.utils.io.readRemaining

/**
 * @author Frank
 * @created 5/13/25
 */

internal fun HttpRequestBuilder.headerMap(): Map<String, List<String>> = this.headers.toMapList()
internal fun HttpRequestBuilder.contentTypeString(): String = this.contentType()?.toString() ?: ""
internal fun HttpRequestBuilder.contentLengthL(): Long = this.contentLength() ?: 0L
internal fun HeadersBuilder.toMapList(): Map<String, List<String>> = entries().associate { it.key to it.value }

internal fun HttpResponse.headerMap(): Map<String, List<String>> = this.headers.toMap()
internal fun HttpResponse.contentTypeString(): String = this.contentType()?.toString() ?: ""
internal fun HttpResponse.contentLengthL(): Long = this.contentLength() ?: 0L

internal fun ContentType?.isProtoBuf(): Boolean = this?.match(ContentType.Application.ProtoBuf) ?: false

/**
 * Tries to read the text from a ByteReadChannel using the specified charset.
 * Returns null if an exception occurs.
 */
internal suspend inline fun ByteReadChannel.tryReadText(charset: Charset): String? = try {
    readRemaining().readText(charset = charset)
} catch (cause: Throwable) {
    null
}

internal fun parseContentType(contentType: String): ContentType? = try {
    ContentType.parse(contentType)
} catch (e: Exception) {
    null
}