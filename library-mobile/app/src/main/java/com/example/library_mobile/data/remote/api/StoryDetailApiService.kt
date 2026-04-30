package com.example.library_mobile.data.remote.api

import com.example.library_mobile.data.remote.dto.ApiResponse
import com.example.library_mobile.data.remote.dto.FavoriteStatusDto
import com.example.library_mobile.data.remote.dto.RatingRequest
import com.example.library_mobile.data.remote.dto.StoryDetailDto
import com.example.library_mobile.data.remote.dto.StoryRatingDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface StoryDetailApiService {

    @GET("api/stories/{id}")
    suspend fun getStoryDetail(
        @Path("id") storyId: Long
    ): Response<ApiResponse<StoryDetailDto>>

    @GET("api/ratings/{storyId}")
    suspend fun getStoryRating(
        @Path("storyId") storyId: Long
    ): Response<ApiResponse<StoryRatingDto>>

    @POST("api/favorites/{storyId}")
    suspend fun addFavorite(
        @Path("storyId") storyId: Long
    ): Response<ApiResponse<FavoriteStatusDto>>

    @DELETE("api/favorites/{storyId}")
    suspend fun removeFavorite(
        @Path("storyId") storyId: Long
    ): Response<ApiResponse<FavoriteStatusDto>>

    @POST("api/ratings")
    suspend fun rateStory(
        @Body request: RatingRequest
    ): Response<ApiResponse<Any>>
}