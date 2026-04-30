package com.example.library_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

// ===== STORY =====
data class StoryCardDto(
    @SerializedName("id")            val id: Long,
    @SerializedName("title")         val title: String,
    @SerializedName("author")        val author: String?,
    @SerializedName("coverImage")    val coverImage: String?,
    @SerializedName("status")        val status: String,   // ONGOING / COMPLETED
    @SerializedName("viewCount")     val viewCount: Long,
    @SerializedName("avgRating")     val avgRating: Double,
    @SerializedName("totalChapters") val totalChapters: Int,
    @SerializedName("genres")        val genres: List<GenreDto>,
    @SerializedName("updatedAt")     val updatedAt: String?
)

data class GenreDto(
    @SerializedName("id")   val id: Long,
    @SerializedName("name") val name: String
)

data class PageResponse<T>(
    @SerializedName("content")       val content: List<T>,
    @SerializedName("pageNumber")    val pageNumber: Int,
    @SerializedName("pageSize")      val pageSize: Int,
    @SerializedName("totalElements") val totalElements: Long,
    @SerializedName("totalPages")    val totalPages: Int,
    @SerializedName("isFirst")       val isFirst: Boolean,
    @SerializedName("isLast")        val isLast: Boolean
)