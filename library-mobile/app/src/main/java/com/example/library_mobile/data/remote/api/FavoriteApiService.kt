package com.example.library_mobile.data.remote.api

import com.example.library_mobile.data.remote.dto.ApiResponse
import com.example.library_mobile.data.remote.dto.FavoriteDto
import com.example.library_mobile.data.remote.dto.FavoriteStatusDto
import com.example.library_mobile.data.remote.dto.PageResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FavoriteApiService {

    @GET("api/favorites")
    suspend fun getFavorites(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<FavoriteDto>>>

    @DELETE("api/favorites/{storyId}")
    suspend fun removeFavorite(
        @Path("storyId") storyId: Long
    ): Response<ApiResponse<FavoriteStatusDto>>
}