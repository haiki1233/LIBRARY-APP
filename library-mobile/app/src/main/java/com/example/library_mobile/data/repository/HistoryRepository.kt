package com.example.library_mobile.data.repository

import com.example.library_mobile.data.remote.api.HistoryApiService
import com.example.library_mobile.data.remote.dto.HistoryDto
import com.example.library_mobile.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoryRepository(private val api: HistoryApiService) {

    suspend fun getHistory(page: Int = 0): Result<Pair<List<HistoryDto>, Boolean>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getHistory(page)
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    Result.Success(Pair(data?.content ?: emptyList(), data?.isLast ?: true))
                } else {
                    Result.Error("Không tải được lịch sử đọc")
                }
            } catch (e: Exception) { Result.Error(mapError(e)) }
        }
    }

    suspend fun deleteHistory(historyId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.deleteHistory(historyId)
                if (response.isSuccessful) Result.Success(Unit)
                else Result.Error("Không xóa được lịch sử")
            } catch (e: Exception) { Result.Error(mapError(e)) }
        }
    }

    private fun mapError(e: Exception) = when (e) {
        is java.net.UnknownHostException   -> "Không có kết nối mạng"
        is java.net.SocketTimeoutException -> "Kết nối quá thời gian"
        else -> "Lỗi không xác định"
    }
}