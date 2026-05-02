package com.example.library_mobile.ui.reading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.library_mobile.data.local.ReadingPreferences
import com.example.library_mobile.data.remote.dto.ChapterDetailDto
import com.example.library_mobile.data.repository.ReadingRepository
import com.example.library_mobile.utils.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ReadingSettings(
    val readMode: ReadingPreferences.ReadMode,
    val fontSize: Float,
    val isDarkMode: Boolean,
    val lineSpacing: Float,
    val fontFamily: ReadingPreferences.FontFamily
)

class ReadingViewModel(
    private val repository: ReadingRepository,
    private val prefs: ReadingPreferences
) : ViewModel() {

    private val _chapterState = MutableLiveData<Result<ChapterDetailDto>>()
    val chapterState: LiveData<Result<ChapterDetailDto>> = _chapterState

    private val _settings = MutableLiveData<ReadingSettings>()
    val settings: LiveData<ReadingSettings> = _settings

    private val _isToolbarVisible = MutableLiveData<Boolean>(true)
    val isToolbarVisible: LiveData<Boolean> = _isToolbarVisible

    private val _isSettingsVisible = MutableLiveData<Boolean>(false)
    val isSettingsVisible: LiveData<Boolean> = _isSettingsVisible

    // Scroll position để save history
    private var currentScrollPosition = 0
    private var currentChapterId: Long = -1L
    private var saveHistoryJob: Job? = null

    init {
        loadSettings()
    }

    // ===== LOAD CHAPTER =====
    fun loadChapter(chapterId: Long) {
        currentChapterId = chapterId
        viewModelScope.launch {
            _chapterState.value = Result.Loading
            _chapterState.value = repository.getChapterDetail(chapterId)
        }
    }

    // ===== TOOLBAR TOGGLE =====
    fun toggleToolbar() {
        _isToolbarVisible.value = !(_isToolbarVisible.value ?: true)
    }

    fun showToolbar() { _isToolbarVisible.value = true }

    // ===== SETTINGS PANEL =====
    fun toggleSettings() {
        _isSettingsVisible.value = !(_isSettingsVisible.value ?: false)
    }

    fun hideSettings() { _isSettingsVisible.value = false }

    // ===== SETTINGS CHANGES =====
    fun setReadMode(mode: ReadingPreferences.ReadMode) {
        prefs.readMode = mode
        loadSettings()
    }

    fun increaseFontSize() {
        prefs.fontSize = (prefs.fontSize + 2f).coerceAtMost(28f)
        loadSettings()
    }

    fun decreaseFontSize() {
        prefs.fontSize = (prefs.fontSize - 2f).coerceAtLeast(12f)
        loadSettings()
    }

    fun toggleDarkMode() {
        prefs.isDarkMode = !prefs.isDarkMode
        loadSettings()
    }

    fun setLineSpacing(spacing: Float) {
        prefs.lineSpacing = spacing
        loadSettings()
    }

    fun setFontFamily(family: ReadingPreferences.FontFamily) {
        prefs.fontFamily = family
        loadSettings()
    }

    private fun loadSettings() {
        _settings.value = ReadingSettings(
            readMode    = prefs.readMode,
            fontSize    = prefs.fontSize,
            isDarkMode  = prefs.isDarkMode,
            lineSpacing = prefs.lineSpacing,
            fontFamily  = prefs.fontFamily
        )
    }

    // ===== SCROLL TRACKING =====
    // Debounce 2s để tránh gọi API liên tục khi đang scroll
    fun onScrollPositionChanged(position: Int) {
        currentScrollPosition = position
        saveHistoryJob?.cancel()
        saveHistoryJob = viewModelScope.launch {
            delay(2000)
            if (currentChapterId != -1L) {
                repository.saveHistory(currentChapterId, position)
            }
        }
    }

    // Force save khi thoát màn hình
    fun saveHistoryNow() {
        saveHistoryJob?.cancel()
        viewModelScope.launch {
            if (currentChapterId != -1L) {
                repository.saveHistory(currentChapterId, currentScrollPosition)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveHistoryNow()
    }

    class Factory(
        private val repository: ReadingRepository,
        private val prefs: ReadingPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ReadingViewModel(repository, prefs) as T
    }
}