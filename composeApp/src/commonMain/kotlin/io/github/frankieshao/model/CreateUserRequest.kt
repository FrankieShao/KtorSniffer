package io.github.frankieshao.model

import kotlinx.serialization.Serializable

/**
 * Request body for creating a new user (used in Protobuf requests).
 */
@Serializable
data class CreateUserRequest(
    val user_name: String,
    val email: String,
    val age: Int,
)

/**
 * Protobuf Response body for a user returned by the API.
 */
@Serializable
data class UserResponse(
    val user_id: String,
    val user_name: String,
    val email: String,
    val age: Int,
    val status: Int
)

/**
 * Generic API response wrapper for user-related endpoints.
 */
@Serializable
data class ApiResponse (
    val success: Boolean,
    val message: String,
    val user: UserResponse? = null
)