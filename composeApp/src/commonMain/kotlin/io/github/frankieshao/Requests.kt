package io.github.frankieshao

import io.github.frankieshao.model.CreateUserRequest
import io.github.frankieshao.model.Echo
import io.github.frankieshao.model.PostRequest
import io.github.frankieshao.model.UserResponse
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

/**
 * Performs a GET request to a sample endpoint and returns the response as a string.
 * @return The response body as a string.
 */
suspend fun get(): String {
    return customHttpClient.request("https://dummyjson.com/carts") {
        method = HttpMethod.Get
    }.body<String>()
}

/**
 * Performs a POST request with a JSON body and returns the response as a JSON string.
 * @return The response body as a JSON string.
 */
suspend fun post(): String {
    val result = customHttpClient.post("https://echo.apifox.com/post") {
        contentType(ContentType.Application.Json)
        setBody(
            PostRequest(
                d = "deserunt",
                dd = "adipisicing enim deserunt Duis"
            )
        )
    }.body<Echo>()
    return Json.encodeToString(result)
}

/**
 * Performs a POST request with a Protobuf body.
 */
suspend fun postProto(): String {
    val request = CreateUserRequest(
        user_name = "John Doe",
        email = "william@domain.com",
        age = 30
    )
    val result = customHttpClient.post("http://localhost:8084/users") {
        contentType(ContentType.Application.ProtoBuf)
        setBody(request)
    }.body<UserResponse>()
    return result.toString()
}