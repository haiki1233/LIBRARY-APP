package com.example.library_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StoryDetailDto(
    @SerializedName("id")            val id: Long,
    @SerializedName("title")         val title: String,
    @SerializedName("author")        val author: String?,
    @SerializedName("description")   val description: String?,
    @SerializedName("coverImage")    val coverImage: String?,
    @SerializedName("status")        val status: String,
    @SerializedName("viewCount")     val viewCount: Long,
    @SerializedName("avgRating")     val avgRating: Double,
    @SerializedName("totalRatings")  val totalRatings: Int,
    @SerializedName("totalChapters") val totalChapters: Int,
    @SerializedName("genres")        val genres: List<GenreDto>,
    @SerializedName("chapters")      val chapters: List<ChapterSummaryDto>,
    @SerializedName("createdAt")     val createdAt: String?,
    @SerializedName("updatedAt")     val updatedAt: String?
)

data class ChapterSummaryDto(
    @SerializedName("id")            val id: Long,
    @SerializedName("chapterNumber") val chapterNumber: Int,
    @SerializedName("title")         val title: String,
    @SerializedName("createdAt")     val createdAt: String?
)

data class StoryRatingDto(
    @SerializedName("storyId")      val storyId: Long,
    @SerializedName("storyTitle")   val storyTitle: String,
    @SerializedName("avgScore")     val avgScore: Double,
    @SerializedName("totalRatings") val totalRatings: Long,
    @SerializedName("distribution") val distribution: RatingDistributionDto,
    @SerializedName("myScore")      val myScore: Int?
)

data class RatingDistributionDto(
    @SerializedName("oneStar")   val oneStar: Long,
    @SerializedName("twoStar")   val twoStar: Long,
    @SerializedName("threeStar") val threeStar: Long,
    @SerializedName("fourStar")  val fourStar: Long,
    @SerializedName("fiveStar")  val fiveStar: Long
)

data class FavoriteStatusDto(
    @SerializedName("storyId")        val storyId: Long,
    @SerializedName("isFavorited")    val isFavorited: Boolean,
    @SerializedName("totalFavorites") val totalFavorites: Long
)

data class RatingRequest(
    @SerializedName("storyId") val storyId: Long,
    @SerializedName("score")   val score: Int
)