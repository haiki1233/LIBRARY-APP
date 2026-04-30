package com.example.library_mobile.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.library_mobile.data.repository.StoryDetailData
import com.example.library_mobile.data.repository.StoryDetailRepository
import com.example.library_mobile.utils.Result
import kotlinx.coroutines.launch

class StoryDetailViewModel(
    private val repository: StoryDetailRepository
) : ViewModel() {

    private val _detailState = MutableLiveData<Result<StoryDetailData>>()
    val detailState: LiveData<Result<StoryDetailData>> = _detailState

    private val _isFavorited    = MutableLiveData<Boolean>(false)
    val isFavorited: LiveData<Boolean> = _isFavorited

    private val _myRating       = MutableLiveData<Int?>(null)
    val myRating: LiveData<Int?> = _myRating

    private val _toastMessage   = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private val _isDescExpanded = MutableLiveData<Boolean>(false)
    val isDescExpanded: LiveData<Boolean> = _isDescExpanded

    fun loadDetail(storyId: Long) {
        viewModelScope.launch {
            _detailState.value = Result.Loading
            val result = repository.getStoryDetail(storyId)
            _detailState.value = result

            if (result is Result.Success) {
                _myRating.value = result.data.rating?.myScore
            }
        }
    }

    fun toggleFavorite(storyId: Long) {
        viewModelScope.launch {
            val current = _isFavorited.value ?: false
            // Optimistic update - đổi UI ngay lập tức
            _isFavorited.value = !current

            val result = if (current) {
                repository.removeFavorite(storyId)
            } else {
                repository.addFavorite(storyId)
            }

            when (result) {
                is Result.Success -> {
                    _isFavorited.value = result.data.isFavorited
                    _toastMessage.value = if (result.data.isFavorited)
                        "Đã thêm vào yêu thích ❤️"
                    else
                        "Đã bỏ khỏi yêu thích"
                }
                is Result.Error -> {
                    // Rollback nếu API fail
                    _isFavorited.value = current
                    _toastMessage.value = result.message
                }
                else -> {}
            }
        }
    }

    fun rateStory(storyId: Long, score: Int) {
        viewModelScope.launch {
            val prevRating = _myRating.value
            _myRating.value = score // Optimistic

            when (val result = repository.rateStory(storyId, score)) {
                is Result.Success -> {
                    _toastMessage.value = "Đánh giá ${"⭐".repeat(score)} thành công!"
                    // Reload để cập nhật avgRating mới
                    loadDetail(storyId)
                }
                is Result.Error -> {
                    _myRating.value = prevRating // Rollback
                    _toastMessage.value = result.message
                }
                else -> {}
            }
        }
    }

    fun toggleDescription() {
        _isDescExpanded.value = !(_isDescExpanded.value ?: false)
    }

    fun clearToast() { _toastMessage.value = null }

    fun setFavorited(favorited: Boolean) { _isFavorited.value = favorited }

    class Factory(private val repository: StoryDetailRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StoryDetailViewModel(repository) as T
    }
}