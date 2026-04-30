package com.example.library_mobile.data.remote.api

import com.example.library_mobile.data.remote.dto.ApiResponse
import com.example.library_mobile.data.remote.dto.GenreDto
import com.example.library_mobile.data.remote.dto.PageResponse
import com.example.library_mobile.data.remote.dto.StoryCardDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SearchApiService {

    // Lấy tất cả thể loại
    @GET("api/genres")
    suspend fun getAllGenres(): Response<ApiResponse<List<GenreDto>>>

    // Tìm kiếm theo keyword
    @GET("api/stories/search")
    suspend fun searchStories(
        @Query("q")    keyword: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<StoryCardDto>>>

    // Lọc theo thể loại
    @GET("api/stories/genre/{genreId}")
    suspend fun getStoriesByGenre(
        @Path("genreId") genreId: Long,
        @Query("page")   page: Int = 0,
        @Query("size")   size: Int = 20
    ): Response<ApiResponse<PageResponse<StoryCardDto>>>
}