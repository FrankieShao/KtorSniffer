package io.github.frankieshao.ktorsniffer.core

import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.api.ClientHook
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.HttpSendPipeline
import io.ktor.client.statement.HttpReceivePipeline
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpResponseContainer
import io.ktor.client.statement.HttpResponsePipeline
import io.ktor.util.pipeline.PipelineContext

/**
 * @author Frank
 * @created 5/15/25
 * Defines a hook that intercepts the HTTP request pipeline before the request is sent.
 * Allows custom logic to be executed before the request is processed.
 */
internal object BeforeRequestHook :
    ClientHook<suspend BeforeRequestHook.Context.(response: HttpRequestBuilder, subject: Any) -> Unit> {

    class Context(private val context: PipelineContext<Any, HttpRequestBuilder>) {
        suspend fun proceed() = context.proceed()
    }

    override fun install(
        client: HttpClient,
        handler: suspend Context.(request: HttpRequestBuilder, subject: Any) -> Unit
    ) {
        client.requestPipeline.intercept(HttpRequestPipeline.Before) { subject ->
            handler(Context(this), context, subject)
        }
    }
}

/**
 * Hook for monitoring the sending phase of the HTTP request pipeline.
 * Allows modification or observation of the request body before it is sent.
 */
internal object SendMonitorHook :
    ClientHook<suspend SendMonitorHook.Context.(response: HttpRequestBuilder) -> Unit> {

    class Context(private val context: PipelineContext<Any, HttpRequestBuilder>) {
        suspend fun proceedWith(content: Any) = context.proceedWith(content)
    }

    override fun install(
        client: HttpClient,
        handler: suspend Context.(request: HttpRequestBuilder) -> Unit
    ) {
        client.sendPipeline.intercept(HttpSendPipeline.Monitoring) {
            handler(Context(this), context)
        }
    }
}

/**
 * Hook for receiving the HTTP response in the response pipeline.
 * Allows custom logic to be executed when a response is received.
 */
object ReceiveResponseHook :
    ClientHook<suspend ReceiveResponseHook.Context.(call: HttpClientCall) -> Unit> {

    class Context(private val context: PipelineContext<HttpResponseContainer, HttpClientCall>) {
        suspend fun proceed() = context.proceed()
    }

    override fun install(
        client: HttpClient,
        handler: suspend Context.(call: HttpClientCall) -> Unit
    ) {
        client.responsePipeline.intercept(HttpResponsePipeline.Receive) {
            handler(Context(this), context)
        }
    }
}

/**
 * Hook for the after-response phase in the response pipeline.
 * Allows custom logic to be executed after the response is processed.
 */
internal object AfterResponseHook :
    ClientHook<suspend AfterResponseHook.Context.(responseContainer: HttpResponseContainer, response: HttpResponse) -> Unit> {

    class Context(private val context: PipelineContext<HttpResponseContainer, HttpClientCall>) {
        suspend fun proceed() = context.proceed()
    }

    override fun install(
        client: HttpClient,
        handler: suspend Context.(responseContainer: HttpResponseContainer, response: HttpResponse) -> Unit
    ) {
        client.responsePipeline.intercept(HttpResponsePipeline.After) { subject ->
            handler(Context(this), subject, context.response)
        }
    }
}

/**
 * Hook for the after-receive phase in the receive pipeline.
 * Allows custom logic to be executed after the response is received and before it is returned to the caller.
 */
internal object AfterReceiveHook :
    ClientHook<suspend AfterReceiveHook.Context.(HttpResponse) -> Unit> {

    class Context(private val context: PipelineContext<HttpResponse, Unit>) {
        suspend fun proceedWith(response: HttpResponse) = context.proceedWith(response)
        suspend fun proceed() = context.proceed()
    }

    override fun install(client: HttpClient, handler: suspend Context.(HttpResponse) -> Unit) {
        client.receivePipeline.intercept(HttpReceivePipeline.After) {
            handler(Context(this), subject)
        }
    }
}

