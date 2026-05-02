package com.example.library_mobile.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.library_mobile.R
import com.example.library_mobile.data.remote.dto.StoryCardDto
import com.example.library_mobile.data.repository.HomeRepository
import com.example.library_mobile.databinding.ActivityHomeBinding
import com.example.library_mobile.ui.adapter.BannerAdapter
import com.example.library_mobile.ui.adapter.SearchAdapter
import com.example.library_mobile.ui.adapter.StoryGridAdapter
import com.example.library_mobile.ui.adapter.StoryHorizontalAdapter
import com.example.library_mobile.ui.favorite.FavoriteActivity
import com.example.library_mobile.ui.search.SearchActivity
import com.example.library_mobile.utils.AppModule
import com.example.library_mobile.utils.Result
import com.example.library_mobile.utils.hide
import com.example.library_mobile.utils.show
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.math.abs

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private val viewModel: HomeViewModel by viewModels {
        val repo = HomeRepository(
            AppModule.provideStoryApiService(this)
        )
        HomeViewModel.Factory(repo)
    }

    // Adapters
    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var hotAdapter: StoryHorizontalAdapter
    private lateinit var newAdapter: StoryGridAdapter
    private lateinit var completedAdapter: StoryGridAdapter
    private lateinit var searchAdapter: SearchAdapter

    // Auto-scroll banner
    private val bannerHandler = Handler(Looper.getMainLooper())
    private val bannerRunnable = Runnable {
        val next = (binding.vpBanner.currentItem + 1) % (bannerAdapter.itemCount)
        binding.vpBanner.currentItem = next
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUserInfo()
        setupBanner()
        setupRecyclerViews()
        setupSearch()
        setupSwipeRefresh()
        setupBottomNav()
        observeViewModel()
    }

    // ===== USER INFO =====
    private fun setupUserInfo() {
        val tokenManager = AppModule.provideTokenManager(this)
        val avatarUrl = tokenManager.getAvatar()
        if (!avatarUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.placeholder_avatar)
                .circleCrop()
                .into(binding.ivAvatar)
        }
        binding.ivAvatar.setOnClickListener {
            // TODO: Navigate to ProfileActivity
        }
    }

    // ===== BANNER =====
    private fun setupBanner() {
        bannerAdapter = BannerAdapter { story -> navigateToDetail(story) }

        binding.vpBanner.apply {
            adapter = bannerAdapter
            offscreenPageLimit = 3

            // Card scale transform
            val transformer = CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(16))
                addTransformer { page, position ->
                    val scale = 1 - abs(position) * 0.1f
                    page.scaleY = scale
                    page.alpha  = 0.5f + (1 - abs(position)) * 0.5f
                }
            }
            setPageTransformer(transformer)

            // Register listener cho auto-scroll
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    restartBannerAutoScroll()
                }
            })
        }
    }

    private fun startBannerAutoScroll() {
        bannerHandler.postDelayed(bannerRunnable, 3000)
    }

    private fun restartBannerAutoScroll() {
        bannerHandler.removeCallbacks(bannerRunnable)
        bannerHandler.postDelayed(bannerRunnable, 3000)
    }

    private fun stopBannerAutoScroll() {
        bannerHandler.removeCallbacks(bannerRunnable)
    }

    // ===== RECYCLER VIEWS =====
    private fun setupRecyclerViews() {

        // Hot stories - Horizontal
        hotAdapter = StoryHorizontalAdapter { story -> navigateToDetail(story) }
        binding.rvHot.apply {
            adapter = hotAdapter
            layoutManager = LinearLayoutManager(
                this@HomeActivity, LinearLayoutManager.HORIZONTAL, false
            )
            setHasFixedSize(true)
        }

        // New stories - Grid 2 cột
        newAdapter = StoryGridAdapter { story -> navigateToDetail(story) }
        binding.rvNew.apply {
            adapter = newAdapter
            layoutManager = GridLayoutManager(this@HomeActivity, 2)
            setHasFixedSize(false)
        }

        // Completed stories - Grid 2 cột
        completedAdapter = StoryGridAdapter { story -> navigateToDetail(story) }
        binding.rvCompleted.apply {
            adapter = completedAdapter
            layoutManager = GridLayoutManager(this@HomeActivity, 2)
            setHasFixedSize(false)
        }

        // Search results - Vertical list
        searchAdapter = SearchAdapter { story -> navigateToDetail(story) }
        binding.rvSearch.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(this@HomeActivity)
        }
    }

    // ===== SEARCH =====
    private fun setupSearch() {
        binding.etSearch.doAfterTextChanged { text ->
            val keyword = text?.toString() ?: ""
            viewModel.search(keyword)

            if (keyword.isEmpty()) {
                showHomeContent()
            } else {
                showSearchResult()
            }
        }

        // IME Search action
        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            val keyword = binding.etSearch.text?.toString() ?: ""
            if (keyword.isNotEmpty()) {
                viewModel.search(keyword)
            }
            true
        }
    }

    // ===== SWIPE REFRESH =====
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.apply {
            setColorSchemeResources(R.color.primary)
            setProgressBackgroundColorSchemeResource(R.color.surface)
            setOnRefreshListener { viewModel.refresh() }
        }

        viewModel.isRefreshing.observe(this) { isRefreshing ->
            binding.swipeRefresh.isRefreshing = isRefreshing
        }
    }

    // ===== BOTTOM NAV =====
    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_home
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home    -> true
                R.id.nav_search  -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    false
                }
                R.id.nav_favorite -> {
                    startActivity(Intent(this, FavoriteActivity::class.java))
                    false
                }
                R.id.nav_history -> {
                    // TODO: Navigate to HistoryActivity
                    true
                }
                R.id.nav_profile -> {
                    // TODO: Navigate to ProfileActivity
                    true
                }
                else -> false
            }
        }
    }

    // ===== OBSERVE VIEWMODEL =====
    private fun observeViewModel() {

        viewModel.homeData.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    showState(State.LOADING)
                }

                is Result.Success -> {
                    showState(State.CONTENT)
                    val data = result.data

                    // Banner
                    bannerAdapter.submitList(data.bannerStories)
                    TabLayoutMediator(binding.tabIndicator, binding.vpBanner) { _, _ -> }.attach()
                    if (data.bannerStories.isNotEmpty()) startBannerAutoScroll()

                    // Lists
                    hotAdapter.submitList(data.hotStories)
                    newAdapter.submitList(data.newStories)
                    completedAdapter.submitList(data.completedStories)

                    // Ẩn section "Truyện Full" nếu rỗng
                    if (data.completedStories.isEmpty()) {
                        binding.layoutCompleted.hide()
                    } else {
                        binding.layoutCompleted.show()
                    }

                    // Fade in animation
                    binding.scrollHome.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
                    )
                }

                is Result.Error -> {
                    showState(State.ERROR)
                    binding.tvErrorMsg.text = result.message
                }
            }
        }

        // Search results
        viewModel.searchResult.observe(this) { result ->
            when (result) {
                null -> {
                    binding.progressSearch.hide()
                    binding.layoutEmptySearch.hide()
                }
                is Result.Loading -> {
                    binding.progressSearch.show()
                    binding.layoutEmptySearch.hide()
                    binding.rvSearch.hide()
                }
                is Result.Success -> {
                    binding.progressSearch.hide()
                    val list = result.data
                    if (list.isEmpty()) {
                        binding.layoutEmptySearch.show()
                        binding.rvSearch.hide()
                        binding.tvEmptySearchMsg.text = getString(R.string.search_no_results, binding.etSearch.text.toString())
                    } else {
                        binding.layoutEmptySearch.hide()
                        binding.rvSearch.show()
                        searchAdapter.submitList(list)
                    }
                }
                is Result.Error -> {
                    binding.progressSearch.hide()
                    binding.layoutEmptySearch.show()
                    binding.tvEmptySearchMsg.text = result.message
                }
            }
        }

        // Retry button
        binding.btnRetry.setOnClickListener {
            viewModel.loadHomeData()
        }

        // See all buttons
        binding.tvSeeAllHot.setOnClickListener {
            // TODO: Navigate to StoryListActivity with sortBy=viewCount
        }
        binding.tvSeeAllNew.setOnClickListener {
            // TODO: Navigate to StoryListActivity with sortBy=updatedAt
        }
        binding.tvSeeAllFull.setOnClickListener {
            // TODO: Navigate to StoryListActivity filter=COMPLETED
        }
    }

    // ===== STATE MANAGEMENT =====
    private enum class State { LOADING, CONTENT, ERROR }

    private fun showState(state: State) {
        binding.layoutLoading.visibility = if (state == State.LOADING) View.VISIBLE else View.GONE
        binding.swipeRefresh.visibility  = if (state == State.CONTENT) View.VISIBLE else View.GONE
        binding.layoutError.visibility   = if (state == State.ERROR)   View.VISIBLE else View.GONE
    }

    private fun showHomeContent() {
        binding.layoutSearchResult.hide()
        binding.swipeRefresh.show()
        binding.etSearch.clearFocus()
    }

    private fun showSearchResult() {
        binding.layoutSearchResult.show()
        binding.swipeRefresh.hide()
    }

    // ===== NAVIGATION =====
    private fun navigateToDetail(story: StoryCardDto) {
        val intent = Intent(this, com.example.library_mobile.ui.detail.StoryDetailActivity::class.java)
            .putExtra(com.example.library_mobile.ui.detail.StoryDetailActivity.EXTRA_STORY_ID, story.id)
        startActivity(intent)
    }

    // ===== LIFECYCLE =====
    override fun onResume() {
        super.onResume()
        if (bannerAdapter.itemCount > 0) startBannerAutoScroll()
    }

    override fun onPause() {
        super.onPause()
        stopBannerAutoScroll()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBannerAutoScroll()
    }
}