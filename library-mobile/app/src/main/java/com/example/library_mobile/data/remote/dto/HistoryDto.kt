package com.example.library_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class HistoryDto(
    @SerializedName("historyId")      val historyId: Long,
    @SerializedName("lastReadAt")     val lastReadAt: String?,
    @SerializedName("scrollPosition") val scrollPosition: Int,
    @SerializedName("chapterId")      val chapterId: Long,
    @SerializedName("chapterNumber")  val chapterNumber: Int,
    @SerializedName("chapterTitle")   val chapterTitle: String,
    @SerializedName("storyId")        val storyId: Long,
    @SerializedName("storyTitle")     val storyTitle: String,
    @SerializedName("storyAuthor")    val storyAuthor: String?,
    @SerializedName("storyCoverImage") val storyCoverImage: String?,
    @SerializedName("storyStatus")    val storyStatus: String,
    @SerializedName("totalChapters")  val totalChapters: Int
)