package com.example.library_mobile.data.remote.api

import com.example.library_mobile.data.remote.dto.ApiResponse
import com.example.library_mobile.data.remote.dto.PageResponse
import com.example.library_mobile.data.remote.dto.StoryCardDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface StoryApiService {

    // Danh sách truyện (có phân trang + sort)
    @GET("api/stories")
    suspend fun getStories(
        @Query("page")   page: Int    = 0,
        @Query("size")   size: Int    = 20,
        @Query("sortBy") sortBy: String = "updatedAt"
    ): Response<ApiResponse<PageResponse<StoryCardDto>>>

    // Truyện hot - sort theo viewCount
    @GET("api/stories")
    suspend fun getHotStories(
        @Query("page")   page: Int = 0,
        @Query("size")   size: Int = 10,
        @Query("sortBy") sortBy: String = "viewCount"
    ): Response<ApiResponse<PageResponse<StoryCardDto>>>

    // Truyện mới - sort theo updatedAt
    @GET("api/stories")
    suspend fun getNewStories(
        @Query("page")   page: Int = 0,
        @Query("size")   size: Int = 20,
        @Query("sortBy") sortBy: String = "updatedAt"
    ): Response<ApiResponse<PageResponse<StoryCardDto>>>

    // Truyện full - filter completed
    @GET("api/stories")
    suspend fun getCompletedStories(
        @Query("page")   page: Int = 0,
        @Query("size")   size: Int = 20,
        @Query("sortBy") sortBy: String = "updatedAt"
    ): Response<ApiResponse<PageResponse<StoryCardDto>>>

    // Tìm kiếm
    @GET("api/stories/search")
    suspend fun searchStories(
        @Query("q")    keyword: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<StoryCardDto>>>
}