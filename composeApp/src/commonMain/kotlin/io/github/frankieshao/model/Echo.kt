package io.github.frankieshao.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing the response from the echo API.
 */
@Serializable
data class Echo(
    @SerialName("data")
    val `data`: String,
    @SerialName("files")
    val files: Files,
    @SerialName("origin")
    val origin: String,
    @SerialName("url")
    val url: String
) {

    @Serializable
    class Files

    @Serializable
    data class Headers(
        @SerialName("Accept")
        val accept: String,
        @SerialName("Accept-Encoding")
        val acceptEncoding: String,
        @SerialName("Connection")
        val connection: String,
        @SerialName("Content-Length")
        val contentLength: String,
        @SerialName("Content-Type")
        val contentType: String,
        @SerialName("Host")
        val host: String,
        @SerialName("User-Agent")
        val userAgent: String
    )
}