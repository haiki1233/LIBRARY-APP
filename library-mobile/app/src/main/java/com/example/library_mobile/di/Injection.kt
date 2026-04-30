package com.example.library_mobile.di

import android.content.Context
import com.example.library_mobile.data.local.TokenManager
import com.example.library_mobile.data.remote.RetrofitClient
import com.example.library_mobile.data.remote.api.AuthApiService
import com.example.library_mobile.data.repository.AuthRepository

object Injection {
    fun provideAuthRepository(context: Context): AuthRepository {
        val tokenManager = TokenManager(context)
        val retrofit = RetrofitClient.createRetrofit(tokenManager)
        val apiService = retrofit.create(AuthApiService::class.java)
        return AuthRepository(apiService, tokenManager)
    }
}