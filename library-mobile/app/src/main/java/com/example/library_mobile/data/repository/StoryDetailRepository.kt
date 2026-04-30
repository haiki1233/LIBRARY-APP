package com.example.library_mobile.data.repository

import com.example.library_mobile.data.remote.api.StoryDetailApiService
import com.example.library_mobile.data.remote.dto.FavoriteStatusDto
import com.example.library_mobile.data.remote.dto.RatingRequest
import com.example.library_mobile.data.remote.dto.StoryDetailDto
import com.example.library_mobile.data.remote.dto.StoryRatingDto
import com.example.library_mobile.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

data class StoryDetailData(
    val story: StoryDetailDto,
    val rating: StoryRatingDto?   // null nếu API rating fail - không block UI
)

class StoryDetailRepository(private val api: StoryDetailApiService) {

    // Load story + rating song song
    suspend fun getStoryDetail(storyId: Long): Result<StoryDetailData> {
        return withContext(Dispatchers.IO) {
            try {
                val storyDeferred  = async { api.getStoryDetail(storyId) }
                val ratingDeferred = async { api.getStoryRating(storyId) }

                val storyResponse  = storyDeferred.await()
                val ratingResponse = ratingDeferred.await()

                if (storyResponse.isSuccessful && storyResponse.body()?.data != null) {
                    Result.Success(
                        StoryDetailData(
                            story  = storyResponse.body()!!.data!!,
                            rating = ratingResponse.body()?.data
                        )
                    )
                } else {
                    Result.Error("Không tải được thông tin truyện")
                }
            } catch (e: Exception) {
                Result.Error(mapError(e))
            }
        }
    }

    suspend fun addFavorite(storyId: Long): Result<FavoriteStatusDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.addFavorite(storyId)
                if (response.isSuccessful && response.body()?.data != null)
                    Result.Success(response.body()!!.data!!)
                else
                    Result.Error(response.body()?.message ?: "Lỗi thêm yêu thích")
            } catch (e: Exception) { Result.Error(mapError(e)) }
        }
    }

    suspend fun removeFavorite(storyId: Long): Result<FavoriteStatusDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.removeFavorite(storyId)
                if (response.isSuccessful && response.body()?.data != null)
                    Result.Success(response.body()!!.data!!)
                else
                    Result.Error(response.body()?.message ?: "Lỗi bỏ yêu thích")
            } catch (e: Exception) { Result.Error(mapError(e)) }
        }
    }

    suspend fun rateStory(storyId: Long, score: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.rateStory(RatingRequest(storyId, score))
                if (response.isSuccessful) Result.Success(Unit)
                else Result.Error("Lỗi đánh giá")
            } catch (e: Exception) { Result.Error(mapError(e)) }
        }
    }

    private fun mapError(e: Exception) = when (e) {
        is java.net.UnknownHostException   -> "Không có kết nối mạng"
        is java.net.SocketTimeoutException -> "Kết nối quá thời gian"
        else -> "Lỗi không xác định"
    }
}