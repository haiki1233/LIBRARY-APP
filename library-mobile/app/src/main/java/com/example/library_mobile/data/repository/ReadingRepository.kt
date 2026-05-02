package com.example.library_mobile.data.repository

import com.example.library_mobile.data.remote.api.ReadingApiService
import com.example.library_mobile.data.remote.dto.ChapterDetailDto
import com.example.library_mobile.data.remote.dto.SaveHistoryRequest
import com.example.library_mobile.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReadingRepository(private val api: ReadingApiService) {

    suspend fun getChapterDetail(chapterId: Long): Result<ChapterDetailDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getChapterDetail(chapterId)
                if (response.isSuccessful && response.body()?.data != null)
                    Result.Success(response.body()!!.data!!)
                else
                    Result.Error("Không tải được chapter")
            } catch (e: Exception) { Result.Error(mapError(e)) }
        }
    }

    // Fire-and-forget: lưu lịch sử không block UI
    suspend fun saveHistory(chapterId: Long, scrollPosition: Int) {
        withContext(Dispatchers.IO) {
            try {
                api.saveHistory(SaveHistoryRequest(chapterId, scrollPosition))
            } catch (_: Exception) { /* Silent - không quan trọng bằng đọc truyện */ }
        }
    }

    private fun mapError(e: Exception) = when (e) {
        is java.net.UnknownHostException   -> "Không có kết nối mạng"
        is java.net.SocketTimeoutException -> "Kết nối quá thời gian"
        else -> "Lỗi tải chapter"
    }
}