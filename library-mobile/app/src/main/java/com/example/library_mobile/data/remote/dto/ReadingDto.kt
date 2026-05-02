package com.example.library_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChapterDetailDto(
    @SerializedName("id")            val id: Long,
    @SerializedName("chapterNumber") val chapterNumber: Int,
    @SerializedName("title")         val title: String,
    @SerializedName("createdAt")     val createdAt: String?,
    @SerializedName("storyId")       val storyId: Long,
    @SerializedName("storyTitle")    val storyTitle: String,
    @SerializedName("images")        val images: List<ChapterImageDto>,
    @SerializedName("navigation")    val navigation: NavigationDto
)

data class ChapterImageDto(
    @SerializedName("id")         val id: Long,
    @SerializedName("imageUrl")   val imageUrl: String,
    @SerializedName("orderIndex") val orderIndex: Int
)

data class NavigationDto(
    @SerializedName("prevChapterId")     val prevChapterId: Long?,
    @SerializedName("prevChapterNumber") val prevChapterNumber: Int?,
    @SerializedName("nextChapterId")     val nextChapterId: Long?,
    @SerializedName("nextChapterNumber") val nextChapterNumber: Int?
)

data class SaveHistoryRequest(
    @SerializedName("chapterId")      val chapterId: Long,
    @SerializedName("scrollPosition") val scrollPosition: Int
)