package com.example.library_mobile.data.remote.api

import com.example.library_mobile.data.remote.dto.ApiResponse
import com.example.library_mobile.data.remote.dto.AuthResponse
import com.example.library_mobile.data.remote.dto.LoginRequest
import com.example.library_mobile.data.remote.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<AuthResponse>>

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<AuthResponse>>
}