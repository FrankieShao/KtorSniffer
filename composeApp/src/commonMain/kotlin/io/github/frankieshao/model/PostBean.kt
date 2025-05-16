package io.github.frankieshao.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for a sample POST request.
 */
@Serializable
data class PostRequest(
    val d: String,
    val dd: String
)

/**
 * Response body for a sample POST request.
 */
@Serializable
data class PostResponse(
    val id: String,
    val name: String,
    val data: PostResponseDetail,
    val createdAt: String
)

/**
 * Detailed data for a sample POST response.
 */
@Serializable
data class PostResponseDetail(
    val year: Int,
    val price: Float,
    @SerialName("CPU model")
    val model: String,
    @SerialName("Hard disk size")
    val disk: String
)
