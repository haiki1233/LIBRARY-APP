package com.example.library_mobile.data.remote.interceptor

import com.example.library_mobile.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Tự động đính kèm Authorization header vào mọi request
 * nếu user đã đăng nhập (có token)
 */
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.getAccessToken()

        val request = if (!token.isNullOrEmpty()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}