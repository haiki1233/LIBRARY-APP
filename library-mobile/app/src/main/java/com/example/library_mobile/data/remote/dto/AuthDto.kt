package com.example.library_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

// ===== REQUEST =====

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

// ===== RESPONSE =====

data class ApiResponse<T>(
    @SerializedName("success")   val success: Boolean,
    @SerializedName("message")   val message: String,
    @SerializedName("data")      val data: T?,
    @SerializedName("timestamp") val timestamp: String?
)

data class AuthResponse(
    @SerializedName("accessToken")  val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("tokenType")    val tokenType: String,
    @SerializedName("expiresIn")    val expiresIn: Long,
    @SerializedName("user")         val user: UserInfo
)

data class UserInfo(
    @SerializedName("id")       val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("email")    val email: String,
    @SerializedName("avatar")   val avatar: String?,
    @SerializedName("role")     val role: String
)