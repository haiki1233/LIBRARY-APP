package com.example.library_mobile.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.library_mobile.R
import com.example.library_mobile.data.remote.dto.HistoryDto
import com.example.library_mobile.data.repository.HistoryRepository
import com.example.library_mobile.databinding.ActivityHistoryBinding
import com.example.library_mobile.ui.adapter.HistoryAdapter
import com.example.library_mobile.ui.detail.StoryDetailActivity
import com.example.library_mobile.ui.reading.ReadingActivity
import com.example.library_mobile.utils.AppModule
import com.example.library_mobile.utils.hide
import com.example.library_mobile.utils.show
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModel.Factory(
            HistoryRepository(AppModule.provideHistoryApiService(this))
        )
    }

    private lateinit var historyAdapter: HistoryAdapter
    private var lastDeletedItem: HistoryDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSwipeRefresh()
        setupClickListeners()
        observeViewModel()
    }

    // ===== SETUP =====
    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            onContinueRead = { item -> navigateToReader(item) },
            onStoryClick   = { item -> navigateToDetail(item) },
            onDelete       = { item -> confirmDelete(item) }
        )

        binding.rvHistory.apply {
            adapter       = historyAdapter
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            setHasFixedSize(false)

            // Pagination
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    val lm          = rv.layoutManager as LinearLayoutManager
                    val lastVisible = lm.findLastVisibleItemPosition()
                    val total       = lm.itemCount
                    if (lastVisible >= total - 3) viewModel.loadMore()
                }
            })
        }

        // Swipe to delete
        historyAdapter.attachSwipeToDelete(
            recyclerView = binding.rvHistory,
            onSwiped     = { item -> confirmDelete(item) }
        )
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.apply {
            setColorSchemeResources(R.color.primary)
            setProgressBackgroundColorSchemeResource(R.color.surface)
            setOnRefreshListener { viewModel.loadHistory() }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnRetry.setOnClickListener { viewModel.loadHistory() }

        binding.btnExplore.setOnClickListener { finish() }

        // Xóa tất cả
        binding.btnClearAll.setOnClickListener { showClearAllDialog() }
    }

    // ===== OBSERVE =====
    private fun observeViewModel() {

        viewModel.state.observe(this) { state ->
            binding.swipeRefresh.isRefreshing = false

            when (state) {
                is HistoryState.Loading -> {
                    showState(State.LOADING)
                    binding.layoutCountBar.hide()
                    binding.btnClearAll.hide()
                }

                is HistoryState.LoadingMore -> { /* giữ list hiện tại */ }

                is HistoryState.Success -> {
                    showState(State.LIST)
                    historyAdapter.submitList(state.items)

                    binding.layoutCountBar.show()
                    binding.tvCount.text = "${state.items.size} truyện đã đọc"
                    binding.btnClearAll.show()
                }

                is HistoryState.Empty -> {
                    showState(State.EMPTY)
                    binding.layoutCountBar.hide()
                    binding.btnClearAll.hide()
                }

                is HistoryState.Error -> {
                    showState(State.ERROR)
                    binding.tvErrorMsg.text = state.message
                    binding.layoutCountBar.hide()
                    binding.btnClearAll.hide()
                }
            }
        }

        // Snackbar sau khi xóa
        viewModel.deletedItem.observe(this) { item ->
            item ?: return@observe
            lastDeletedItem = item
            viewModel.clearDeletedItem()

            Snackbar.make(
                binding.root,
                "Đã xóa lịch sử \"${item.storyTitle}\"",
                Snackbar.LENGTH_LONG
            ).apply {
                setAction("HOÀN TÁC") {
                    // TODO: Implement undo - gọi lại API saveHistory
                    Toast.makeText(
                        this@HistoryActivity,
                        "Tính năng hoàn tác đang phát triển 🚧",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.loadHistory() // Reload để restore
                }
                setActionTextColor(getColor(R.color.primary))
                setBackgroundTint(getColor(R.color.surface))
                setTextColor(getColor(R.color.on_background))
                show()
            }
        }

        viewModel.toastMessage.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                viewModel.clearToast()
            }
        }
    }

    // ===== DIALOGS =====
    private fun confirmDelete(item: HistoryDto) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Xóa lịch sử?")
            .setMessage("Xóa \"${item.storyTitle}\" khỏi lịch sử đọc?")
            .setNegativeButton("Huỷ") { dialog, _ ->
                dialog.dismiss()
                historyAdapter.notifyDataSetChanged() // Restore swipe
            }
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.deleteHistory(item)
            }
            .setOnCancelListener {
                historyAdapter.notifyDataSetChanged()
            }
            .show()
    }

    private fun showClearAllDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("⚠️ Xóa toàn bộ lịch sử?")
            .setMessage("Hành động này không thể hoàn tác. Toàn bộ lịch sử đọc sẽ bị xóa.")
            .setNegativeButton("Huỷ", null)
            .setPositiveButton("Xóa tất cả") { _, _ ->
                // Xóa tuần tự từng item
                val currentList = historyAdapter.currentList.toList()
                currentList.forEach { item ->
                    viewModel.deleteHistory(item)
                }
                Toast.makeText(this, "Đã xóa toàn bộ lịch sử", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    // ===== STATE =====
    private enum class State { LOADING, LIST, EMPTY, ERROR }

    private fun showState(state: State) {
        binding.layoutLoading.visibility = if (state == State.LOADING) View.VISIBLE else View.GONE
        binding.swipeRefresh.visibility  = if (state == State.LIST)    View.VISIBLE else View.GONE
        binding.layoutEmpty.visibility   = if (state == State.EMPTY)   View.VISIBLE else View.GONE
        binding.layoutError.visibility   = if (state == State.ERROR)   View.VISIBLE else View.GONE
    }

    // ===== NAVIGATION =====
    private fun navigateToReader(item: HistoryDto) {
        // TODO: Navigate to ReadingActivity
         startActivity(
             Intent(this, ReadingActivity::class.java)
                 .putExtra(ReadingActivity.EXTRA_CHAPTER_ID, item.chapterId)
                 .putExtra(ReadingActivity.EXTRA_STORY_ID, item.storyId)
         )
    }

    private fun navigateToDetail(item: HistoryDto) {
        // TODO: Navigate to StoryDetailActivity
         startActivity(
             Intent(this, StoryDetailActivity::class.java)
                 .putExtra(StoryDetailActivity.EXTRA_STORY_ID, item.storyId)
         )
    }
}