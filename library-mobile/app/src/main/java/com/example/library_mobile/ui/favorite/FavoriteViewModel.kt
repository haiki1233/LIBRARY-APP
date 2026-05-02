package com.example.library_mobile.ui.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.library_mobile.data.remote.dto.FavoriteDto
import com.example.library_mobile.data.repository.FavoriteRepository
import com.example.library_mobile.utils.Result
import kotlinx.coroutines.launch

sealed class FavoriteState {
    object Loading : FavoriteState()
    object LoadingMore : FavoriteState()
    data class Success(val items: List<FavoriteDto>, val isLastPage: Boolean) : FavoriteState()
    object Empty : FavoriteState()
    data class Error(val message: String) : FavoriteState()
}

class FavoriteViewModel(private val repository: FavoriteRepository) : ViewModel() {

    private val _state = MutableLiveData<FavoriteState>()
    val state: LiveData<FavoriteState> = _state

    private val _removedStoryId = MutableLiveData<Long?>()
    val removedStoryId: LiveData<Long?> = _removedStoryId

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    // Accumulated list cho pagination
    private val allItems = mutableListOf<FavoriteDto>()
    private var currentPage = 0
    private var isLastPage  = false

    init { loadFavorites() }

    fun loadFavorites() {
        viewModelScope.launch {
            currentPage = 0
            isLastPage  = false
            allItems.clear()
            _state.value = FavoriteState.Loading

            when (val result = repository.getFavorites(0)) {
                is Result.Success -> {
                    val (list, last) = result.data
                    isLastPage = last
                    allItems.addAll(list)
                    _state.value = if (list.isEmpty()) FavoriteState.Empty
                    else FavoriteState.Success(allItems.toList(), isLastPage)
                }
                is Result.Error -> _state.value = FavoriteState.Error(result.message)
                else -> {}
            }
        }
    }

    fun loadMore() {
        if (isLastPage || _state.value is FavoriteState.LoadingMore) return
        viewModelScope.launch {
            _state.value = FavoriteState.LoadingMore
            currentPage++
            when (val result = repository.getFavorites(currentPage)) {
                is Result.Success -> {
                    val (list, last) = result.data
                    isLastPage = last
                    allItems.addAll(list)
                    _state.value = FavoriteState.Success(allItems.toList(), isLastPage)
                }
                is Result.Error -> {
                    currentPage--
                    _state.value = FavoriteState.Success(allItems.toList(), isLastPage)
                }
                else -> {}
            }
        }
    }

    fun removeFavorite(storyId: Long) {
        // Optimistic update: xóa khỏi list ngay lập tức
        val prevList = allItems.toList()
        allItems.removeAll { it.storyId == storyId }
        _state.value = if (allItems.isEmpty()) FavoriteState.Empty
        else FavoriteState.Success(allItems.toList(), isLastPage)

        viewModelScope.launch {
            when (val result = repository.removeFavorite(storyId)) {
                is Result.Success -> {
                    _removedStoryId.value = storyId
                    _toastMessage.value = "Đã bỏ khỏi yêu thích"
                }
                is Result.Error -> {
                    // Rollback
                    allItems.clear()
                    allItems.addAll(prevList)
                    _state.value = FavoriteState.Success(allItems.toList(), isLastPage)
                    _toastMessage.value = result.message
                }
                else -> {}
            }
        }
    }

    fun clearToast() { _toastMessage.value = null }

    class Factory(private val repository: FavoriteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FavoriteViewModel(repository) as T
    }
}