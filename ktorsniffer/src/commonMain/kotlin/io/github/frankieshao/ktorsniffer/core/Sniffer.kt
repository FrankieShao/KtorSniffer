package io.github.frankieshao.ktorsniffer.core

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.ClientPluginBuilder
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.observer.wrapWithContent
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.http.charset
import io.ktor.http.cio.Response
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.util.AttributeKey
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.split
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.KtorDsl
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.discard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * @author Frank
 * @created 5/13/25
 */

private val LogId: AttributeKey<String> = AttributeKey("LogId")
private val LogSaver: AttributeKey<NetworkLogSaver> = AttributeKey("LogSaver")
private val DefaultSnifferScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

/**
 * Configuration class for the Sniffer plugin. Allows customization of ProtoBuf usage and coroutine scope.
 */
@KtorDsl
class SnifferConfig {
    /** Optional ProtoBuf instance for serializing/deserializing ProtoBuf bodies. */
    var protoBuf: ProtoBuf? = ProtoBuf()
    /** Coroutine scope used for asynchronous operations in the plugin. */
    var coroutineScope: CoroutineScope = DefaultSnifferScope

    fun isProtoBufEnabled(): Boolean {
        return protoBuf != null
    }

    val protoBufBodyFormatter: ProtoBufBodyFormatter
        get() = requireNotNull(protoBuf) { "ProtoBuf body formatter not set" }.bodyFormatter

    fun coroutineScope(block: () -> CoroutineScope) {
        coroutineScope = block()
    }

    fun protoBuf(block: ProtoBuf.() -> Unit) {
        protoBuf?.block()
    }
}

/**
 * Ktor client plugin for logging and saving network requests and responses.
 * Intercepts various phases of the HTTP client pipeline to capture and persist network logs.
 */
val Sniffer: ClientPlugin<SnifferConfig> = createClientPlugin("Sniffer", ::SnifferConfig) {

    on(BeforeRequestHook) { request: HttpRequestBuilder, subject: Any ->
        // Assign a unique log ID and create a new log saver for this request
        request.attributes.put(LogId, generateId())
        val networkLogSaver = newSaver()
        request.attributes.put(LogSaver, networkLogSaver)

        // Save ProtoBuf request if enabled and content type matches
        if (pluginConfig.isProtoBufEnabled() && request.contentType().isProtoBuf()) {
            networkLogSaver.saveRequest(
                RequestSaveEntry(
                    url = request.url.toString(),
                    method = request.method.value,
                    headers = request.headerMap(),
                    bodyType = request.contentTypeString(),
                    body = pluginConfig.protoBufBodyFormatter(request.bodyType, subject),
                    bodyLength = request.contentLengthL()
                )
            )
        }
        // Proceed with the request pipeline
        try {
            proceed()
        } catch (cause: Throwable) {
            throw cause
        } finally {
        }
    }

    on(SendMonitorHook) { request: HttpRequestBuilder ->
        // Attempt to save non-ProtoBuf request body
        val loggedRequest = try {
            saveRequest(request)
        } catch (_: Throwable) {
            null
        }

        try {
            proceedWith(loggedRequest ?: request.body)
        } catch (cause: Throwable) {
            throw cause
        } finally {
        }
    }

    on(AfterReceiveHook) { response ->
        // Save non-ProtoBuf response body if applicable
        if (!response.contentType().isProtoBuf()) {
            val newResponse = saveResponse(response)
            proceedWith(newResponse)
        } else {
            proceed()
        }
    }

    on(ReceiveResponseHook) { call ->
        val networkLogSaver = call.attributes[LogSaver]
        val response: HttpResponse = call.response
        try {
            proceed()
        } catch (cause: Throwable) {
            // Capture any exception thrown in the receive response phase
            networkLogSaver.responseException(cause.message ?: cause.toString())
            throw cause
        } finally {
            // Save ProtoBuf response if enabled
            if (pluginConfig.isProtoBufEnabled() && response.contentType().isProtoBuf()) {
                networkLogSaver.saveResponse(
                    ResponseSaveEntry(
                        statusCode = response.status.value,
                        headers = response.headerMap(),
                        bodyType = response.contentTypeString(),
                        body = "",
                        protocol = response.version.toString(),
                        bodyLength = response.contentLengthL(),
                        requestTime = response.requestTime.timestamp,
                        responseTime = response.responseTime.timestamp
                    )
                )
            }
            // Finalize and persist the log asynchronously
            pluginConfig.coroutineScope.launch {
                networkLogSaver.finish(call.attributes[LogId])
            }
        }
    }

    on(AfterResponseHook) { responseContainer, response ->
        val networkLogSaver = response.call.attributes[LogSaver]
        val id = response.call.attributes[LogId]
        // Save ProtoBuf response if enabled
        if (pluginConfig.isProtoBufEnabled() && response.contentType().isProtoBuf()) {
            networkLogSaver.saveResponse(
                ResponseSaveEntry(
                    statusCode = response.status.value,
                    headers = response.headerMap(),
                    bodyType = response.contentTypeString(),
                    body = pluginConfig.protoBufBodyFormatter(
                        responseContainer.expectedType,
                        responseContainer.response
                    ),
                    protocol = response.version.toString(),
                    bodyLength = response.contentLengthL(),
                    requestTime = response.requestTime.timestamp,
                    responseTime = response.responseTime.timestamp
                )
            )
            // Finalize and persist the log asynchronously
            pluginConfig.coroutineScope.launch {
                networkLogSaver.finish(id)
            }
        }

        try {
            proceed()
        } catch (cause: Throwable) {
            throw cause
        } finally {
        }
    }

}

/**
 * Saves the request body for logging, wrapping the content if necessary for observation.
 * Only non-ProtoBuf requests are logged here.
 */
private suspend fun ClientPluginBuilder<SnifferConfig>.saveRequest(
    request: HttpRequestBuilder
): OutgoingContent {
    val networkLogSaver = request.attributes[LogSaver]
    val content = request.body as OutgoingContent
    val charset = content.contentType?.charset() ?: Charsets.UTF_8
    val contentType = content.contentType?.withoutParameters()
    val contentLength = content.contentLength
    val channel = ByteChannel()
    pluginConfig.coroutineScope.launch {
        val body = channel.tryReadText(charset) ?: "[request body omitted]"
        if (!request.contentType().isProtoBuf()) {
            networkLogSaver.saveRequest(
                RequestSaveEntry(
                    url = request.url.toString(),
                    method = request.method.value,
                    headers = request.headerMap(),
                    bodyType = contentType.toString(),
                    body = body,
                    bodyLength = contentLength ?: 0
                )
            )
        }
    }
    return content.observe(channel)
}

/**
 * Splits the response content for logging and returns a new HttpResponse with the observed content.
 * The side response is used for logging, while the new response is returned to the pipeline.
 */
@OptIn(InternalAPI::class)
private fun ClientPluginBuilder<SnifferConfig>.saveResponse(
    response: HttpResponse
): HttpResponse {
    val (loggingContent, responseContent) = response.rawContent.split(response)
    val newResponse = response.call.wrapWithContent(responseContent).response
    val sideResponse = response.call.wrapWithContent(loggingContent).response
    val networkLogSaver = response.call.attributes[LogSaver]
    val id = response.call.attributes[LogId]
    pluginConfig.coroutineScope.launch {
        val content = sideResponse.rawContent
        val responseHeaders = sideResponse.headerMap()
        val responseBodyType = sideResponse.contentTypeString()
        val responseBodyLength = sideResponse.contentLengthL()
        val protocol = sideResponse.version.toString()

        runCatching {
            val body =
                content.tryReadText(sideResponse.contentType()?.charset() ?: Charsets.UTF_8)
                    ?: "[response body omitted]"
            networkLogSaver.saveResponse(
                ResponseSaveEntry(
                    statusCode = sideResponse.status.value,
                    headers = responseHeaders,
                    bodyType = responseBodyType,
                    body = body,
                    protocol = protocol,
                    bodyLength = responseBodyLength,
                    requestTime = response.requestTime.timestamp,
                    responseTime = response.responseTime.timestamp
                )
            )
        }

        if (!content.isClosedForRead) {
            runCatching { content.discard() }
        }
    }
    return newResponse
}
