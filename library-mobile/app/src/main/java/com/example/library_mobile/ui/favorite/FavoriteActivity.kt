package com.example.library_mobile.ui.favorite

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.library_mobile.R
import com.example.library_mobile.data.remote.dto.FavoriteDto
import com.example.library_mobile.data.repository.FavoriteRepository
import com.example.library_mobile.databinding.ActivityFavoriteBinding
import com.example.library_mobile.ui.adapter.FavoriteAdapter
import com.example.library_mobile.ui.detail.StoryDetailActivity
import com.example.library_mobile.utils.AppModule
import com.example.library_mobile.utils.hide
import com.example.library_mobile.utils.show
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class FavoriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteBinding

    private val viewModel: FavoriteViewModel by viewModels {
        FavoriteViewModel.Factory(
            FavoriteRepository(AppModule.provideFavoriteApiService(this))
        )
    }

    private lateinit var favoriteAdapter: FavoriteAdapter

    // Lưu item vừa xóa để Undo
    private var lastRemovedItem: FavoriteDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSwipeRefresh()
        setupClickListeners()
        observeViewModel()
    }

    // ===== SETUP =====
    private fun setupRecyclerView() {
        favoriteAdapter = FavoriteAdapter(
            onClick  = { item -> navigateToDetail(item) },
            onRemove = { item -> confirmRemove(item) }
        )

        binding.rvFavorites.apply {
            adapter       = favoriteAdapter
            layoutManager = LinearLayoutManager(this@FavoriteActivity)
            setHasFixedSize(false)

            // Load more khi scroll gần cuối
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    val lm          = rv.layoutManager as LinearLayoutManager
                    val lastVisible = lm.findLastVisibleItemPosition()
                    val total       = lm.itemCount
                    if (lastVisible >= total - 3) viewModel.loadMore()
                }
            })
        }

        // Swipe to delete gắn vào RecyclerView
        favoriteAdapter.attachSwipeToDelete(
            recyclerView = binding.rvFavorites,
            onSwiped     = { item -> confirmRemove(item) }
        )
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.apply {
            setColorSchemeResources(R.color.primary)
            setProgressBackgroundColorSchemeResource(R.color.surface)
            setOnRefreshListener {
                viewModel.loadFavorites()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnRetry.setOnClickListener { viewModel.loadFavorites() }

        // Khám phá truyện khi list trống
        binding.btnExplore.setOnClickListener {
            // TODO: Navigate to HomeActivity
            finish()
        }

        // Sort - hiện dialog chọn cách sắp xếp
        binding.btnSort.setOnClickListener {
            showSortDialog()
        }
    }

    // ===== OBSERVE VIEWMODEL =====
    private fun observeViewModel() {

        viewModel.state.observe(this) { state ->
            // Ẩn swipe refresh indicator
            binding.swipeRefresh.isRefreshing = false

            when (state) {
                is FavoriteState.Loading -> {
                    showState(State.LOADING)
                    binding.layoutCountBar.hide()
                }

                is FavoriteState.LoadingMore -> {
                    // Giữ list hiện tại, không ẩn gì
                }

                is FavoriteState.Success -> {
                    showState(State.LIST)
                    favoriteAdapter.submitList(state.items)

                    // Cập nhật số lượng
                    binding.layoutCountBar.show()
                    binding.tvCount.text = "${state.items.size} truyện đã lưu"
                }

                is FavoriteState.Empty -> {
                    showState(State.EMPTY)
                    binding.layoutCountBar.hide()
                }

                is FavoriteState.Error -> {
                    showState(State.ERROR)
                    binding.tvErrorMsg.text = state.message
                    binding.layoutCountBar.hide()
                }
            }
        }

        viewModel.toastMessage.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                viewModel.clearToast()
            }
        }

        // Hiện Snackbar Undo sau khi xóa thành công
        viewModel.removedStoryId.observe(this) { storyId ->
            storyId ?: return@observe
            val removed = lastRemovedItem ?: return@observe

            Snackbar.make(
                binding.root,
                "Đã bỏ \"${removed.storyTitle}\" khỏi yêu thích",
                Snackbar.LENGTH_LONG
            ).apply {
                setAction("HOÀN TÁC") {
                    // TODO: Gọi addFavorite(storyId) để undo
                    // viewModel.addFavorite(storyId)
                    Toast.makeText(
                        this@FavoriteActivity,
                        "Tính năng hoàn tác đang phát triển 🚧",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                setActionTextColor(getColor(R.color.primary))
                setBackgroundTint(getColor(R.color.surface))
                setTextColor(getColor(R.color.on_background))
                show()
            }
        }
    }

    // ===== CONFIRM REMOVE DIALOG =====
    private fun confirmRemove(item: FavoriteDto) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Bỏ yêu thích?")
            .setMessage("Bỏ \"${item.storyTitle}\" khỏi danh sách yêu thích?")
            .setNegativeButton("Huỷ") { dialog, _ ->
                dialog.dismiss()
                // Refresh list để restore item bị swipe
                favoriteAdapter.notifyDataSetChanged()
            }
            .setPositiveButton("Bỏ yêu thích") { _, _ ->
                lastRemovedItem = item
                viewModel.removeFavorite(item.storyId)
            }
            .setOnCancelListener {
                // Restore nếu cancel bằng back press
                favoriteAdapter.notifyDataSetChanged()
            }
            .show()
    }

    // ===== SORT DIALOG =====
    private fun showSortDialog() {
        val options = arrayOf(
            "Mới lưu nhất",
            "Cũ lưu nhất",
            "Mới cập nhật nhất",
            "Tên A → Z"
        )
        var selected = 0

        MaterialAlertDialogBuilder(this)
            .setTitle("Sắp xếp theo")
            .setSingleChoiceItems(options, selected) { _, which -> selected = which }
            .setNegativeButton("Huỷ", null)
            .setPositiveButton("Áp dụng") { _, _ ->
                val currentList = favoriteAdapter.currentList.toMutableList()
                val sorted = when (selected) {
                    0 -> currentList.sortedByDescending { it.savedAt }
                    1 -> currentList.sortedBy { it.savedAt }
                    2 -> currentList.sortedByDescending { it.storyUpdatedAt }
                    3 -> currentList.sortedBy { it.storyTitle }
                    else -> currentList
                }
                favoriteAdapter.submitList(sorted)
                Toast.makeText(this, "Đã sắp xếp: ${options[selected]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    // ===== STATE MANAGEMENT =====
    private enum class State { LOADING, LIST, EMPTY, ERROR }

    private fun showState(state: State) {
        binding.layoutLoading.visibility = if (state == State.LOADING) View.VISIBLE else View.GONE
        binding.swipeRefresh.visibility  = if (state == State.LIST)    View.VISIBLE else View.GONE
        binding.layoutEmpty.visibility   = if (state == State.EMPTY)   View.VISIBLE else View.GONE
        binding.layoutError.visibility   = if (state == State.ERROR)   View.VISIBLE else View.GONE
    }

    // ===== NAVIGATION =====
    private fun navigateToDetail(item: FavoriteDto) {
        // TODO: navigate to StoryDetailActivity
         startActivity(
             Intent(this, StoryDetailActivity::class.java)
                 .putExtra(StoryDetailActivity.EXTRA_STORY_ID, item.storyId)
         )
    }
}