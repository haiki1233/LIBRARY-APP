package com.example.library_mobile.data.remote.api

import com.example.library_mobile.data.remote.dto.ApiResponse
import com.example.library_mobile.data.remote.dto.HistoryDto
import com.example.library_mobile.data.remote.dto.PageResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HistoryApiService {

    @GET("api/history")
    suspend fun getHistory(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<HistoryDto>>>

    @DELETE("api/history/{id}")
    suspend fun deleteHistory(
        @Path("id") historyId: Long
    ): Response<ApiResponse<Void>>
}