package com.example.library_mobile.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.library_mobile.data.remote.dto.HistoryDto
import com.example.library_mobile.data.repository.HistoryRepository
import com.example.library_mobile.utils.Result
import kotlinx.coroutines.launch

sealed class HistoryState {
    object Loading : HistoryState()
    object LoadingMore : HistoryState()
    data class Success(val items: List<HistoryDto>, val isLastPage: Boolean) : HistoryState()
    object Empty : HistoryState()
    data class Error(val message: String) : HistoryState()
}

class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {

    private val _state = MutableLiveData<HistoryState>()
    val state: LiveData<HistoryState> = _state

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    // Track item vừa xóa để Undo
    private val _deletedItem = MutableLiveData<HistoryDto?>()
    val deletedItem: LiveData<HistoryDto?> = _deletedItem

    private val allItems   = mutableListOf<HistoryDto>()
    private var currentPage = 0
    private var isLastPage  = false

    init { loadHistory() }

    fun loadHistory() {
        viewModelScope.launch {
            currentPage = 0
            isLastPage  = false
            allItems.clear()
            _state.value = HistoryState.Loading

            when (val result = repository.getHistory(0)) {
                is Result.Success -> {
                    val (list, last) = result.data
                    isLastPage = last
                    allItems.addAll(list)
                    _state.value = if (list.isEmpty()) HistoryState.Empty
                    else HistoryState.Success(allItems.toList(), isLastPage)
                }
                is Result.Error -> _state.value = HistoryState.Error(result.message)
                else -> {}
            }
        }
    }

    fun loadMore() {
        if (isLastPage || _state.value is HistoryState.LoadingMore) return
        viewModelScope.launch {
            _state.value = HistoryState.LoadingMore
            currentPage++
            when (val result = repository.getHistory(currentPage)) {
                is Result.Success -> {
                    val (list, last) = result.data
                    isLastPage = last
                    allItems.addAll(list)
                    _state.value = HistoryState.Success(allItems.toList(), isLastPage)
                }
                is Result.Error -> {
                    currentPage--
                    _state.value = HistoryState.Success(allItems.toList(), isLastPage)
                }
                else -> {}
            }
        }
    }

    fun deleteHistory(item: HistoryDto) {
        // Optimistic update
        val prevList = allItems.toList()
        allItems.removeAll { it.historyId == item.historyId }
        _state.value = if (allItems.isEmpty()) HistoryState.Empty
        else HistoryState.Success(allItems.toList(), isLastPage)

        viewModelScope.launch {
            when (val result = repository.deleteHistory(item.historyId)) {
                is Result.Success -> {
                    _deletedItem.value = item
                }
                is Result.Error -> {
                    // Rollback
                    allItems.clear()
                    allItems.addAll(prevList)
                    _state.value = HistoryState.Success(allItems.toList(), isLastPage)
                    _toastMessage.value = result.message
                }
                else -> {}
            }
        }
    }

    fun clearToast()       { _toastMessage.value = null }
    fun clearDeletedItem() { _deletedItem.value = null }

    class Factory(private val repository: HistoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HistoryViewModel(repository) as T
    }
}