package com.example.library_mobile.data.remote

import com.example.library_mobile.data.local.TokenManager
import com.example.library_mobile.data.remote.interceptor.AuthInterceptor
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ⚠️ Đổi BASE_URL theo môi trường
    // Emulator  → "http://10.0.2.2:8080/"
    // Device    → "http://192.168.x.x:8080/"  (IP máy tính trong cùng mạng)
    // Production → "https://yourdomain.com/"
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Chỉ log body khi debug, production dùng NONE
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Tạo OkHttpClient có auth interceptor
     */
    fun createOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))  // Tự động gắn Bearer token
            .addInterceptor(loggingInterceptor)             // Log request/response
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Tạo Retrofit instance
     */
    fun createRetrofit(tokenManager: TokenManager): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient(tokenManager))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}