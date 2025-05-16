package io.github.frankieshao.ktorsniffer.core

import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.util.AttributeKey
import io.ktor.util.copyToBoth
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import io.ktor.utils.io.writer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

/**
 * @author Frank
 * @created 5/13/25
 * Observes the outgoing content and writes it to the provided log channel.
 * Returns a new OutgoingContent that allows the body to be observed as it is sent.
 */
internal suspend fun OutgoingContent.observe(log: ByteWriteChannel): OutgoingContent = when (this) {
    is OutgoingContent.ByteArrayContent -> {
        log.writeFully(bytes())
        log.flushAndClose()
        this
    }

    is OutgoingContent.ReadChannelContent -> {
        val responseChannel = ByteChannel()
        val content = readFrom()

        content.copyToBoth(log, responseChannel)
        LoggedContent(this, responseChannel)
    }

    is OutgoingContent.WriteChannelContent -> {
        val responseChannel = ByteChannel()
        val content = toReadChannel()
        content.copyToBoth(log, responseChannel)
        LoggedContent(this, responseChannel)
    }

    is OutgoingContent.ContentWrapper -> copy(delegate().observe(log))
    is OutgoingContent.NoContent, is OutgoingContent.ProtocolUpgrade -> {
        log.flushAndClose()
        this
    }
}

private fun OutgoingContent.WriteChannelContent.toReadChannel(): ByteReadChannel =
    GlobalScope.writer(Dispatchers.Default) {
        writeTo(channel)
    }.channel

internal class LoggedContent(
    private val originalContent: OutgoingContent,
    private val channel: ByteReadChannel
) : OutgoingContent.ReadChannelContent() {

    override val contentType: ContentType? = originalContent.contentType
    override val contentLength: Long? = originalContent.contentLength
    override val status: HttpStatusCode? = originalContent.status
    override val headers: Headers = originalContent.headers

    override fun <T : Any> getProperty(key: AttributeKey<T>): T? = originalContent.getProperty(key)

    override fun <T : Any> setProperty(key: AttributeKey<T>, value: T?) =
        originalContent.setProperty(key, value)

    override fun readFrom(): ByteReadChannel = channel
}