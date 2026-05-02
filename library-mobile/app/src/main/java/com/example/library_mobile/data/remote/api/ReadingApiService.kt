package com.example.library_mobile.data.remote.api

import com.example.library_mobile.data.remote.dto.ApiResponse
import com.example.library_mobile.data.remote.dto.ChapterDetailDto
import com.example.library_mobile.data.remote.dto.SaveHistoryRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReadingApiService {

    @GET("api/chapter/{id}")
    suspend fun getChapterDetail(
        @Path("id") chapterId: Long
    ): Response<ApiResponse<ChapterDetailDto>>

    @POST("api/history")
    suspend fun saveHistory(
        @Body request: SaveHistoryRequest
    ): Response<ApiResponse<Any>>
}