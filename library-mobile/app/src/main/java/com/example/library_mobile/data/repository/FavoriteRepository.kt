package com.example.library_mobile.data.repository

import com.example.library_mobile.data.remote.api.FavoriteApiService
import com.example.library_mobile.data.remote.dto.FavoriteDto
import com.example.library_mobile.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FavoriteRepository(private val api: FavoriteApiService) {

    suspend fun getFavorites(page: Int = 0): Result<Pair<List<FavoriteDto>, Boolean>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getFavorites(page)
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    Result.Success(Pair(data?.content ?: emptyList(), data?.isLast ?: true))
                } else {
                    Result.Error("Không tải được danh sách yêu thích")
                }
            } catch (e: Exception) { Result.Error(mapError(e)) }
        }
    }

    suspend fun removeFavorite(storyId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.removeFavorite(storyId)
                if (response.isSuccessful) Result.Success(Unit)
                else Result.Error(response.body()?.message ?: "Lỗi bỏ yêu thích")
            } catch (e: Exception) { Result.Error(mapError(e)) }
        }
    }

    private fun mapError(e: Exception) = when (e) {
        is java.net.UnknownHostException   -> "Không có kết nối mạng"
        is java.net.SocketTimeoutException -> "Kết nối quá thời gian"
        else -> "Lỗi không xác định"
    }
}