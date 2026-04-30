package com.example.library_mobile.data.repository

import com.example.library_mobile.data.remote.api.SearchApiService
import com.example.library_mobile.data.remote.dto.GenreDto
import com.example.library_mobile.data.remote.dto.StoryCardDto
import com.example.library_mobile.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchRepository(private val api: SearchApiService) {

    // Cache genres để không gọi API nhiều lần
    private var cachedGenres: List<GenreDto>? = null

    suspend fun getGenres(): Result<List<GenreDto>> = withContext(Dispatchers.IO) {
        cachedGenres?.let { return@withContext Result.Success(it) }
        try {
            val response = api.getAllGenres()
            if (response.isSuccessful) {
                val list = response.body()?.data ?: emptyList()
                cachedGenres = list
                Result.Success(list)
            } else {
                Result.Error("Không tải được thể loại")
            }
        } catch (e: Exception) {
            Result.Error(mapError(e))
        }
    }

    suspend fun search(
        keyword: String,
        page: Int = 0
    ): Result<Pair<List<StoryCardDto>, Boolean>> = withContext(Dispatchers.IO) {
        try {
            val response = api.searchStories(keyword, page)
            if (response.isSuccessful) {
                val data = response.body()?.data
                Result.Success(Pair(data?.content ?: emptyList(), data?.isLast ?: true))
            } else {
                Result.Error("Tìm kiếm thất bại")
            }
        } catch (e: Exception) {
            Result.Error(mapError(e))
        }
    }

    suspend fun filterByGenre(
        genreId: Long,
        page: Int = 0
    ): Result<Pair<List<StoryCardDto>, Boolean>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getStoriesByGenre(genreId, page)
            if (response.isSuccessful) {
                val data = response.body()?.data
                Result.Success(Pair(data?.content ?: emptyList(), data?.isLast ?: true))
            } else {
                Result.Error("Lọc thể loại thất bại")
            }
        } catch (e: Exception) {
            Result.Error(mapError(e))
        }
    }

    private fun mapError(e: Exception) = when (e) {
        is java.net.UnknownHostException   -> "Không có kết nối mạng"
        is java.net.SocketTimeoutException -> "Kết nối quá thời gian"
        else                               -> "Lỗi không xác định"
    }
}