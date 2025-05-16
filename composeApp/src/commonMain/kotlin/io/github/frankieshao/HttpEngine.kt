package io.github.frankieshao

import io.github.frankieshao.ktorsniffer.core.Sniffer
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * Sample HttpClient for using Sniffer plugin.
 */
@OptIn(ExperimentalSerializationApi::class)
val customHttpClient = HttpClient(getDefaultEngine()) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
        protobuf(ProtoBuf {
            encodeDefaults = true
        })
    }
    install(Sniffer)
}