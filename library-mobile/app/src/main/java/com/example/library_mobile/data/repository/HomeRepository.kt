package com.example.library_mobile.data.repository

import com.example.library_mobile.data.remote.api.StoryApiService
import com.example.library_mobile.data.remote.dto.StoryCardDto
import com.example.library_mobile.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

data class HomeData(
    val bannerStories: List<StoryCardDto>,   // Top 5 truyện hot cho banner
    val hotStories: List<StoryCardDto>,       // Horizontal list - hot
    val newStories: List<StoryCardDto>,       // Grid list - mới
    val completedStories: List<StoryCardDto>  // Grid list - full
)

class HomeRepository(private val api: StoryApiService) {

    // Load tất cả data Home song song (async)
    suspend fun getHomeData(): Result<HomeData> {
        return withContext(Dispatchers.IO) {
            try {
                // Gọi 2 API song song để giảm thời gian chờ
                val hotDeferred  = async { api.getHotStories(size = 10) }
                val newDeferred  = async { api.getNewStories(size = 20) }

                val hotResponse  = hotDeferred.await()
                val newResponse  = newDeferred.await()

                val hotList = hotResponse.body()?.data?.content ?: emptyList()
                val newList = newResponse.body()?.data?.content ?: emptyList()

                Result.Success(
                    HomeData(
                        bannerStories    = hotList.take(5),  // 5 truyện đầu làm banner
                        hotStories       = hotList,
                        newStories       = newList,
                        completedStories = newList.filter { it.status == "COMPLETED" }
                    )
                )
            } catch (e: Exception) {
                Result.Error(
                    when (e) {
                        is java.net.UnknownHostException   -> "Không có kết nối mạng"
                        is java.net.SocketTimeoutException -> "Kết nối quá thời gian"
                        else                               -> "Lỗi tải dữ liệu"
                    }
                )
            }
        }
    }

    suspend fun searchStories(keyword: String): Result<List<StoryCardDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.searchStories(keyword)
                if (response.isSuccessful) {
                    Result.Success(response.body()?.data?.content ?: emptyList())
                } else {
                    Result.Error("Tìm kiếm thất bại")
                }
            } catch (e: Exception) {
                Result.Error("Lỗi kết nối")
            }
        }
    }
}