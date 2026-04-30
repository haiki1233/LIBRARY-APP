package com.example.library_mobile.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.library_mobile.data.remote.dto.StoryCardDto
import com.example.library_mobile.data.repository.HomeData
import com.example.library_mobile.data.repository.HomeRepository
import com.example.library_mobile.utils.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {

    private val _homeData    = MutableLiveData<Result<HomeData>>()
    val homeData: LiveData<Result<HomeData>> = _homeData

    private val _searchResult = MutableLiveData<Result<List<StoryCardDto>>?>()
    val searchResult: LiveData<Result<List<StoryCardDto>>?> = _searchResult

    private val _isRefreshing = MutableLiveData<Boolean>(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private var searchJob: Job? = null

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _homeData.value = Result.Loading
            _homeData.value = repository.getHomeData()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _homeData.value = repository.getHomeData()
            _isRefreshing.value = false
        }
    }

    // Debounce search - chờ 500ms sau khi user ngừng gõ mới gọi API
    fun search(keyword: String) {
        searchJob?.cancel()

        if (keyword.isBlank()) {
            _searchResult.value = null
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)
            _searchResult.value = Result.Loading
            _searchResult.value = repository.searchStories(keyword)
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _searchResult.value = null
    }

    // Factory
    class Factory(private val repository: HomeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(repository) as T
    }
}