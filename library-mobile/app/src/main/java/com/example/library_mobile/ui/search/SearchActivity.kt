package com.example.library_mobile.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.library_mobile.R
import com.example.library_mobile.data.local.RecentSearchManager
import com.example.library_mobile.data.remote.dto.StoryCardDto
import com.example.library_mobile.data.repository.SearchRepository
import com.example.library_mobile.databinding.ActivitySearchBinding
import com.example.library_mobile.ui.adapter.GenreChipAdapter
import com.example.library_mobile.ui.adapter.RecentSearchAdapter
import com.example.library_mobile.ui.adapter.StoryGridAdapter
import com.example.library_mobile.utils.AppModule
import com.example.library_mobile.utils.hide
import com.example.library_mobile.utils.show
import com.google.android.material.chip.Chip
import androidx.activity.OnBackPressedCallback

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding

    private val viewModel: SearchViewModel by viewModels {
        val repo = SearchRepository(AppModule.provideSearchApiService(this))
        SearchViewModel.Factory(repo)
    }

    // 1. Khai báo callback xử lý nút Back
    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            // Logic khi người dùng nhấn Back mà đang ở trạng thái tìm kiếm
            binding.etSearch.setText("")
            viewModel.clearSearch()
        }
    }

    private lateinit var genreAdapter: GenreChipAdapter
    private lateinit var resultAdapter: StoryGridAdapter
    private lateinit var recentAdapter: RecentSearchAdapter
    private lateinit var recentSearchManager: RecentSearchManager

    // Cờ để tránh loop khi setText programmatically
    private var isProgrammaticChange = false

    // Từ khóa hiện tại để retry
    private var lastKeyword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recentSearchManager = RecentSearchManager(this)

        setupGenreChips()
        setupResultList()
        setupRecentSearch()
        setupSearchInput()
        setupSuggestions()
        setupClickListeners()
        observeViewModel()

        // 2. Đăng ký callback này vào Dispatcher
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        // Tự focus và mở bàn phím khi vào màn hình
        binding.etSearch.requestFocus()
        showKeyboard()
    }

    // ===== GENRE CHIPS =====
    private fun setupGenreChips() {
        genreAdapter = GenreChipAdapter { genre ->
            // Xóa text search khi chọn genre
            if (genre != null) {
                isProgrammaticChange = true
                binding.etSearch.setText("")
                isProgrammaticChange = false
                viewModel.filterByGenre(genre)
            } else {
                viewModel.clearSearch()
            }
        }

        binding.rvGenres.apply {
            adapter = genreAdapter
            layoutManager = LinearLayoutManager(
                this@SearchActivity, LinearLayoutManager.HORIZONTAL, false
            )
            setHasFixedSize(true)
        }

        // Observe genres
        viewModel.genres.observe(this) { genres ->
            genreAdapter.submitList(genres)
        }
    }

    // ===== RESULT LIST =====
    private fun setupResultList() {
        resultAdapter = StoryGridAdapter { story -> navigateToDetail(story) }

        binding.rvResults.apply {
            adapter = resultAdapter
            layoutManager = GridLayoutManager(this@SearchActivity, 2)

            // Pagination - load more khi scroll gần cuối
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(rv, dx, dy)
                    val layoutManager = rv.layoutManager as GridLayoutManager
                    val totalItems    = layoutManager.itemCount
                    val lastVisible   = layoutManager.findLastVisibleItemPosition()

                    // Load more khi còn 4 item trước khi hết
                    if (lastVisible >= totalItems - 4) {
                        viewModel.loadMore()
                    }
                }
            })
        }
    }

    // ===== RECENT SEARCH =====
    private fun setupRecentSearch() {
        recentAdapter = RecentSearchAdapter(
            onClick = { keyword ->
                binding.etSearch.setText(keyword)
                binding.etSearch.setSelection(keyword.length)
                viewModel.search(keyword)
                hideKeyboard()
            },
            onDelete = { keyword ->
                recentSearchManager.removeSearch(keyword)
                refreshRecentSearch()
            }
        )

        binding.rvRecentSearch.apply {
            adapter = recentAdapter
            layoutManager = LinearLayoutManager(this@SearchActivity)
        }

        refreshRecentSearch()
    }

    private fun refreshRecentSearch() {
        val recentList = recentSearchManager.getRecentSearches()
        recentAdapter.submitList(recentList)
        binding.tvNoRecent.visibility = if (recentList.isEmpty()) View.VISIBLE else View.GONE
    }

    // ===== SUGGESTIONS (static chips) =====
    private val suggestions = listOf(
        "Tiên hiệp", "Huyền huyễn", "Đô thị",
        "Ngôn tình", "Kiếm hiệp", "Võng du"
    )

    private fun setupSuggestions() {
        suggestions.forEach { keyword ->
            val chip = Chip(this).apply {
                text = keyword
                isClickable = true
                isCheckable = false
                setChipBackgroundColorResource(R.color.surface_light)
                setTextColor(getColor(R.color.on_surface))
                chipStrokeWidth = 1f
                setChipStrokeColorResource(R.color.divider)
                chipCornerRadius = 20f

                setOnClickListener {
                    isProgrammaticChange = true
                    binding.etSearch.setText(keyword)
                    isProgrammaticChange = false
                    binding.etSearch.setSelection(keyword.length)
                    viewModel.search(keyword)
                    hideKeyboard()
                }
            }
            binding.chipGroupSuggestions.addView(chip)
        }
    }

    // ===== SEARCH INPUT =====
    private fun setupSearchInput() {
        binding.etSearch.doAfterTextChanged { text ->
            if (isProgrammaticChange) return@doAfterTextChanged
            
            val keyword = text?.toString() ?: ""
            if (keyword == lastKeyword) return@doAfterTextChanged
            
            lastKeyword = keyword

            if (keyword.isBlank()) {
                // Reset về Idle state
                genreAdapter.setSelected(null)
                viewModel.clearSearch()
            } else {
                viewModel.search(keyword)
            }
        }

        // Nhấn Search trên bàn phím
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val keyword = binding.etSearch.text?.toString() ?: ""
                if (keyword.isNotBlank()) {
                    recentSearchManager.addSearch(keyword)
                    hideKeyboard()
                }
                true
            } else false
        }
    }

    // ===== CLICK LISTENERS =====
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnRetry.setOnClickListener {
            if (lastKeyword.isNotBlank()) {
                viewModel.search(lastKeyword)
            } else {
                viewModel.filterByGenre(viewModel.selectedGenre.value)
            }
        }
    }

    // ===== OBSERVE VIEWMODEL =====
    private fun observeViewModel() {
        viewModel.searchState.observe(this) { state ->
            // Nếu state khác Idle, ta bật callback để chặn nút Back.
            // Nếu là Idle, ta tắt nó đi để nút Back hoạt động mặc định (thoát màn hình).
            backPressedCallback.isEnabled = state !is SearchState.Idle
            when (state) {

                is SearchState.Idle -> {
                    showState(State.IDLE)
                    refreshRecentSearch()
                    binding.layoutResultHeader.hide()
                }

                is SearchState.Loading -> {
                    showState(State.LOADING)
                    binding.layoutResultHeader.hide()
                }

                is SearchState.LoadingMore -> {
                    // Không ẩn list, chỉ hiện loading ở cuối
                    // RecyclerView tự handle qua Footer item (có thể thêm sau)
                }

                is SearchState.Success -> {
                    showState(State.RESULT)
                    resultAdapter.submitList(state.stories)

                    // Header: số kết quả
                    binding.layoutResultHeader.show()
                    val selectedGenre = viewModel.selectedGenre.value
                    binding.tvResultCount.text = when {
                        selectedGenre != null ->
                            "${state.stories.size}+ truyện thể loại \"${selectedGenre.name}\""
                        lastKeyword.isNotBlank() ->
                            "Tìm thấy ${state.stories.size}+ kết quả cho \"$lastKeyword\""
                        else -> "${state.stories.size}+ truyện"
                    }
                }

                is SearchState.Empty -> {
                    showState(State.EMPTY)
                    binding.tvEmptyMsg.text = state.message
                    binding.layoutResultHeader.hide()
                }

                is SearchState.Error -> {
                    showState(State.ERROR)
                    binding.tvErrorMsg.text = state.message
                    binding.layoutResultHeader.hide()
                }
            }
        }

        // Sync chip selection với genre filter
        viewModel.selectedGenre.observe(this) { genre ->
            genreAdapter.setSelected(genre?.id)
        }
    }

    // ===== STATE MANAGEMENT =====
    private enum class State { IDLE, LOADING, RESULT, EMPTY, ERROR }

    private fun showState(state: State) {
        binding.layoutIdle.visibility    = if (state == State.IDLE)    View.VISIBLE else View.GONE
        binding.layoutLoading.visibility = if (state == State.LOADING) View.VISIBLE else View.GONE
        binding.rvResults.visibility     = if (state == State.RESULT)  View.VISIBLE else View.GONE
        binding.layoutEmpty.visibility   = if (state == State.EMPTY)   View.VISIBLE else View.GONE
        binding.layoutError.visibility   = if (state == State.ERROR)   View.VISIBLE else View.GONE
    }

    // ===== NAVIGATION =====
    private fun navigateToDetail(story: StoryCardDto) {
        // Lưu vào recent search nếu đang search bằng keyword
        if (lastKeyword.isNotBlank()) {
            recentSearchManager.addSearch(lastKeyword)
        }
        val intent = Intent(this, com.example.library_mobile.ui.detail.StoryDetailActivity::class.java)
            .putExtra(com.example.library_mobile.ui.detail.StoryDetailActivity.EXTRA_STORY_ID, story.id)
        startActivity(intent)
    }

    // ===== KEYBOARD =====
    private fun showKeyboard() {
        binding.etSearch.post {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
        binding.etSearch.clearFocus()
    }
}