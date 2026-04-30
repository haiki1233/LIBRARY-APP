package com.example.library_mobile.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.library_mobile.data.remote.dto.GenreDto
import com.example.library_mobile.data.remote.dto.StoryCardDto
import com.example.library_mobile.data.repository.SearchRepository
import com.example.library_mobile.utils.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Trạng thái của màn hình search
sealed class SearchState {
    object Idle : SearchState()                          // Mới vào, chưa tìm gì
    object Loading : SearchState()                       // Đang tải lần đầu
    object LoadingMore : SearchState()                   // Đang load thêm (pagination)
    data class Success(val stories: List<StoryCardDto>,
                       val isLastPage: Boolean) : SearchState()
    data class Empty(val message: String) : SearchState()
    data class Error(val message: String) : SearchState()
}

class SearchViewModel(private val repository: SearchRepository) : ViewModel() {

    // ===== GENRES =====
    private val _genres = MutableLiveData<List<GenreDto>>()
    val genres: LiveData<List<GenreDto>> = _genres

    // ===== SEARCH STATE =====
    private val _searchState = MutableLiveData<SearchState>(SearchState.Idle)
    val searchState: LiveData<SearchState> = _searchState

    // ===== CURRENT FILTER =====
    private val _selectedGenre = MutableLiveData<GenreDto?>(null)
    val selectedGenre: LiveData<GenreDto?> = _selectedGenre

    // Pagination
    private var currentPage  = 0
    private var currentKeyword = ""
    private var isLastPage   = false
    private val accumulatedList = mutableListOf<StoryCardDto>()

    private var searchJob: Job? = null

    init {
        loadGenres()
    }

    // ===== GENRES =====
    private fun loadGenres() {
        viewModelScope.launch {
            when (val result = repository.getGenres()) {
                is Result.Success -> _genres.value = result.data
                else -> { /* Silent fail - genres không quan trọng bằng search */ }
            }
        }
    }

    // ===== SEARCH với debounce =====
    fun search(keyword: String) {
        searchJob?.cancel()

        // Reset genre filter khi tìm kiếm
        _selectedGenre.value = null

        if (keyword.isBlank()) {
            _searchState.value = SearchState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(400) // Debounce 400ms

            // Reset pagination
            currentPage    = 0
            currentKeyword = keyword
            isLastPage     = false
            accumulatedList.clear()

            _searchState.value = SearchState.Loading

            when (val result = repository.search(keyword, 0)) {
                is Result.Success -> {
                    val (list, last) = result.data
                    isLastPage = last
                    accumulatedList.addAll(list)

                    _searchState.value = if (list.isEmpty())
                        SearchState.Empty("Không tìm thấy kết quả cho \"$keyword\"")
                    else
                        SearchState.Success(accumulatedList.toList(), isLastPage)
                }
                is Result.Error -> _searchState.value = SearchState.Error(result.message)
                else -> {}
            }
        }
    }

    // ===== FILTER BY GENRE =====
    fun filterByGenre(genre: GenreDto?) {
        searchJob?.cancel()
        _selectedGenre.value = genre

        if (genre == null) {
            _searchState.value = SearchState.Idle
            return
        }

        viewModelScope.launch {
            currentPage = 0
            isLastPage  = false
            accumulatedList.clear()

            _searchState.value = SearchState.Loading

            when (val result = repository.filterByGenre(genre.id, 0)) {
                is Result.Success -> {
                    val (list, last) = result.data
                    isLastPage = last
                    accumulatedList.addAll(list)

                    _searchState.value = if (list.isEmpty())
                        SearchState.Empty("Không có truyện thể loại \"${genre.name}\"")
                    else
                        SearchState.Success(accumulatedList.toList(), isLastPage)
                }
                is Result.Error -> _searchState.value = SearchState.Error(result.message)
                else -> {}
            }
        }
    }

    // ===== LOAD MORE (Pagination) =====
    fun loadMore() {
        if (isLastPage || _searchState.value is SearchState.LoadingMore) return

        viewModelScope.launch {
            _searchState.value = SearchState.LoadingMore
            currentPage++

            val result = if (_selectedGenre.value != null) {
                repository.filterByGenre(_selectedGenre.value!!.id, currentPage)
            } else {
                repository.search(currentKeyword, currentPage)
            }

            when (result) {
                is Result.Success -> {
                    val (list, last) = result.data
                    isLastPage = last
                    accumulatedList.addAll(list)
                    _searchState.value = SearchState.Success(accumulatedList.toList(), isLastPage)
                }
                is Result.Error -> {
                    currentPage-- // Rollback
                    _searchState.value = SearchState.Success(accumulatedList.toList(), isLastPage)
                }
                else -> {}
            }
        }
    }

    // ===== CLEAR =====
    fun clearSearch() {
        searchJob?.cancel()
        currentPage = 0
        currentKeyword = ""
        isLastPage  = false
        accumulatedList.clear()
        _selectedGenre.value = null
        _searchState.value = SearchState.Idle
    }

    // Factory
    class Factory(private val repository: SearchRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SearchViewModel(repository) as T
    }
}