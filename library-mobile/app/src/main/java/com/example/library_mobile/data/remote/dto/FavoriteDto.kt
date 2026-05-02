package com.example.library_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FavoriteDto(
    @SerializedName("favoriteId")    val favoriteId: Long,
    @SerializedName("savedAt")       val savedAt: String?,
    @SerializedName("storyId")       val storyId: Long,
    @SerializedName("storyTitle")    val storyTitle: String,
    @SerializedName("storyAuthor")   val storyAuthor: String?,
    @SerializedName("storyCoverImage") val storyCoverImage: String?,
    @SerializedName("storyStatus")   val storyStatus: String,
    @SerializedName("totalChapters") val totalChapters: Int,
    @SerializedName("avgRating")     val avgRating: Double,
    @SerializedName("genres")        val genres: List<GenreDto>,
    @SerializedName("storyUpdatedAt") val storyUpdatedAt: String?
)