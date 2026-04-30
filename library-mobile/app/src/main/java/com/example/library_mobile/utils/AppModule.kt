package com.example.library_mobile.utils

import android.content.Context
import com.example.library_mobile.data.local.TokenManager
import com.example.library_mobile.data.remote.RetrofitClient
import com.example.library_mobile.data.remote.api.AuthApiService
import com.example.library_mobile.data.remote.api.SearchApiService
import com.example.library_mobile.data.remote.api.StoryApiService
import com.example.library_mobile.data.remote.api.StoryDetailApiService
import com.example.library_mobile.data.repository.AuthRepository

object AppModule {

    @Volatile private var tokenManager: TokenManager? = null
    @Volatile private var authRepository: AuthRepository? = null

    fun provideTokenManager(context: Context): TokenManager {
        return tokenManager ?: synchronized(this) {
            TokenManager(context.applicationContext).also { tokenManager = it }
        }
    }

    fun provideAuthRepository(context: Context): AuthRepository {
        return authRepository ?: synchronized(this) {
            val tm      = provideTokenManager(context)
            val retrofit = RetrofitClient.createRetrofit(tm)
            val api     = retrofit.create(AuthApiService::class.java)
            AuthRepository(api, tm).also { authRepository = it }
        }
    }

    // ===== Story API =====
    fun provideStoryApiService(context: Context): StoryApiService {
        val tm      = provideTokenManager(context)
        val retrofit = RetrofitClient.createRetrofit(tm)
        return retrofit.create(StoryApiService::class.java)
    }

    // ===== Search API =====
    fun provideSearchApiService(context: Context): SearchApiService {
        val tm      = provideTokenManager(context)
        val retrofit = RetrofitClient.createRetrofit(tm)
        return retrofit.create(SearchApiService::class.java)
    }

    // ===== Story Detail API =====
    fun provideStoryDetailApiService(context: Context): StoryDetailApiService {
        val tm      = provideTokenManager(context)
        val retrofit = RetrofitClient.createRetrofit(tm)
        return retrofit.create(StoryDetailApiService::class.java)
    }
}
