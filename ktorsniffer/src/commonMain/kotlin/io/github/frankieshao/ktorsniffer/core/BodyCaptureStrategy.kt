package io.github.frankieshao.ktorsniffer.core

import io.ktor.http.ContentType

/**
 * Enum representing the type of body capture strategy.
 * TEXT: Capture the body as text.
 * CUSTOM: Use a custom capture strategy (e.g., for ProtoBuf).
 * SKIP: Do not capture the body (e.g., for binary or large content).
 *
 * Deprecated: This strategy is no longer recommended for use.
 */
@Deprecated("")
enum class BodyCaptureType {
    TEXT,
    CUSTOM,
    SKIP
}

/**
 * Interface for defining a strategy to determine how to capture the body based on content type.
 *
 * Deprecated: This strategy is no longer recommended for use.
 */
@Deprecated("")
interface BodyCaptureStrategy {
    operator fun invoke(contentType: ContentType?): BodyCaptureType
}

/**
 * Default implementation of BodyCaptureStrategy.
 * Determines the capture type based on common content types.
 *
 * Deprecated: This strategy is no longer recommended for use.
 */
@Deprecated("")
class DefaultBodyCaptureStrategy : BodyCaptureStrategy {
    override fun invoke(contentType: ContentType?): BodyCaptureType {
        return when {
            contentType == null -> BodyCaptureType.SKIP
            contentType.match(ContentType.Application.Json) -> BodyCaptureType.TEXT
            contentType.match(ContentType.Application.Xml) -> BodyCaptureType.TEXT
            contentType.match(ContentType.Application.FormUrlEncoded) -> BodyCaptureType.TEXT
            contentType.match(ContentType.Text.Any) -> BodyCaptureType.TEXT
            contentType.match(ContentType.Application.ProtoBuf) -> BodyCaptureType.CUSTOM
            contentType.match(ContentType.MultiPart.Any) -> BodyCaptureType.SKIP // Matches multipart/*
            contentType.match(ContentType.Image.Any) -> BodyCaptureType.SKIP     // Matches image/*
            contentType.match(ContentType.Audio.Any) -> BodyCaptureType.SKIP     // Matches audio/*
            contentType.match(ContentType.Video.Any) -> BodyCaptureType.SKIP     // Matches video/*
            contentType.match(ContentType.Image.JPEG) -> BodyCaptureType.SKIP
            contentType.match(ContentType.Image.PNG) -> BodyCaptureType.SKIP
            // Default for other unhandled application/* or other types
            // Consider if you want to attempt to log these as text or skip them too.
            // For example, application/octet-stream is binary and shouldn't be logged as text.
            contentType.match(ContentType.Application.OctetStream) -> BodyCaptureType.SKIP
            contentType.match(ContentType.Application.Pdf) -> BodyCaptureType.SKIP
            else -> BodyCaptureType.TEXT
        }
    }
}