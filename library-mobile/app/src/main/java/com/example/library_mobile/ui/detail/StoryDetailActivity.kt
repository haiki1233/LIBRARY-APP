package com.example.library_mobile.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.library_mobile.R
import com.example.library_mobile.data.remote.dto.ChapterSummaryDto
import com.example.library_mobile.data.remote.dto.StoryDetailDto
import com.example.library_mobile.data.remote.dto.StoryRatingDto
import com.example.library_mobile.data.repository.StoryDetailRepository
import com.example.library_mobile.databinding.ActivityStoryDetailBinding
import com.example.library_mobile.ui.adapter.ChapterListAdapter
import com.example.library_mobile.utils.AppModule
import com.example.library_mobile.utils.Result
import com.example.library_mobile.utils.show
import com.google.android.material.chip.Chip

class StoryDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STORY_ID = "story_id"
    }

    private lateinit var binding: ActivityStoryDetailBinding

    private val viewModel: StoryDetailViewModel by viewModels {
        val repo = StoryDetailRepository(AppModule.provideStoryDetailApiService(this))
        StoryDetailViewModel.Factory(repo)
    }

    private lateinit var chapterAdapter: ChapterListAdapter
    private var storyId: Long = -1L
    private var chapters: List<ChapterSummaryDto> = emptyList()
    private var isSortAsc = true  // true = tăng dần, false = giảm dần

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storyId = intent.getLongExtra(EXTRA_STORY_ID, -1L)
        if (storyId == -1L) { finish(); return }

        setupChapterList()
        setupClickListeners()
        observeViewModel()

        viewModel.loadDetail(storyId)
    }

    // ===== SETUP =====
    private fun setupChapterList() {
        chapterAdapter = ChapterListAdapter { chapter -> navigateToReader(chapter) }
        binding.rvChapters.apply {
            adapter = chapterAdapter
            layoutManager = LinearLayoutManager(this@StoryDetailActivity)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {

        binding.btnBack.setOnClickListener { finish() }

        binding.btnFavorite.setOnClickListener {
            viewModel.toggleFavorite(storyId)
            // Scale animation
            binding.btnFavorite.animate()
                .scaleX(1.3f).scaleY(1.3f).setDuration(120)
                .withEndAction {
                    binding.btnFavorite.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }.start()
        }

        binding.btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                val story = (viewModel.detailState.value as? Result.Success)?.data?.story
                putExtra(Intent.EXTRA_TEXT,
                    "Đọc truyện '${story?.title}' trên TruyệnHay!")
            }
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ truyện"))
        }

        // Description expand/collapse
        binding.tvExpandDesc.setOnClickListener {
            viewModel.toggleDescription()
        }

        // Sort chapters
        binding.btnSortChapters.setOnClickListener {
            isSortAsc = !isSortAsc
            val sorted = if (isSortAsc) chapters else chapters.reversed()
            chapterAdapter.submitList(sorted)
            binding.btnSortChapters.setImageResource(
                if (isSortAsc) R.drawable.ic_sort_asc else R.drawable.ic_sort_desc
            )
        }

        // Đọc từ đầu
        binding.btnReadFirst.setOnClickListener {
            val firstChapter = chapters.minByOrNull { it.chapterNumber }
            firstChapter?.let { navigateToReader(it) }
        }

        // Đọc tiếp
        binding.btnContinueRead.setOnClickListener {
            Toast.makeText(this, "Tính năng lịch sử đang được phát triển", Toast.LENGTH_SHORT).show()
        }

        // Star rating buttons
        val starButtons = listOf(
            binding.btnStar1, binding.btnStar2, binding.btnStar3,
            binding.btnStar4, binding.btnStar5
        )
        starButtons.forEachIndexed { index, btn ->
            btn.setOnClickListener {
                val score = index + 1
                viewModel.rateStory(storyId, score)
                updateStarUI(score)
            }
        }

        binding.btnRetry.setOnClickListener { viewModel.loadDetail(storyId) }
    }

    // ===== OBSERVE =====
    private fun observeViewModel() {

        viewModel.detailState.observe(this) { result ->
            when (result) {
                is Result.Loading -> showState(State.LOADING)

                is Result.Success -> {
                    showState(State.CONTENT)
                    bindStory(result.data.story)
                    result.data.rating?.let { bindRating(it) }
                }

                is Result.Error -> {
                    showState(State.ERROR)
                    binding.tvErrorMsg.text = result.message
                }
            }
        }

        viewModel.isFavorited.observe(this) { favorited ->
            binding.btnFavorite.setImageResource(
                if (favorited) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            )
            binding.btnFavorite.setColorFilter(
                getColor(if (favorited) R.color.primary else R.color.on_background)
            )
        }

        viewModel.myRating.observe(this) { score ->
            score?.let { updateStarUI(it) }
        }

        viewModel.isDescExpanded.observe(this) { expanded ->
            if (expanded) {
                binding.tvDescription.maxLines = Int.MAX_VALUE
                binding.tvDescription.ellipsize = null
                binding.tvExpandDesc.setText(R.string.collapse_desc)
            } else {
                binding.tvDescription.maxLines = 4
                binding.tvDescription.ellipsize = android.text.TextUtils.TruncateAt.END
                binding.tvExpandDesc.setText(R.string.expand_desc)
            }
        }

        viewModel.toastMessage.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                viewModel.clearToast()
            }
        }
    }

    // ===== BIND DATA =====
    private fun bindStory(story: StoryDetailDto) {
        // Cover image - load cả bg blur và card
        Glide.with(this)
            .load(story.coverImage)
            .placeholder(R.drawable.placeholder_cover)
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.ivCoverBg)

        Glide.with(this)
            .load(story.coverImage)
            .placeholder(R.drawable.placeholder_cover)
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.ivCover)

        // Text info
        binding.tvTitle.text   = story.title
        binding.tvAuthor.text  = story.author ?: "Đang cập nhật"
        binding.tvChapters.text = getString(R.string.chapter_count_format, story.totalChapters)
        binding.tvViews.text   = getString(R.string.view_count_format, formatCount(story.viewCount))

        // Status badge
        binding.tvStatus.text = if (story.status == "COMPLETED") "Full" else "Đang ra"
        binding.tvStatus.setBackgroundResource(
            if (story.status == "COMPLETED") R.drawable.bg_badge_completed
            else R.drawable.bg_badge_ongoing
        )

        // Description
        binding.tvDescription.text = story.description ?: "Chưa có mô tả"

        // Genre chips
        binding.chipGroupGenres.removeAllViews()
        story.genres.take(3).forEach { genre ->
            val chip = Chip(this).apply {
                text = genre.name
                isClickable = false
                isCheckable = false
                setChipBackgroundColorResource(R.color.surface_light)
                setTextColor(getColor(R.color.primary))
                chipStrokeWidth = 1f
                setChipStrokeColorResource(R.color.primary)
                shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                    .setAllCornerSizes(16f)
                    .build()
                textSize = 11f
            }
            binding.chipGroupGenres.addView(chip)
        }

        // Chapter list
        chapters = story.chapters
        chapterAdapter.submitList(story.chapters)

        // Hiện nút "Đọc ngay" nếu có chapter
        if (story.chapters.isNotEmpty()) {
            binding.btnReadFirst.show()
        }

        // Fade in animation
        binding.scrollContent.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
        )
    }

    private fun bindRating(rating: StoryRatingDto) {
        binding.tvAvgScore.text      = "%.1f".format(rating.avgScore)
        binding.tvStarsDisplay.text  = buildStarString(rating.avgScore)
        binding.tvTotalRatings.text  = "${formatCount(rating.totalRatings)} đánh giá"

        // Distribution bars
        val total = rating.totalRatings.coerceAtLeast(1)
        binding.bar5star.setProgressCompat(((rating.distribution.fiveStar  * 100) / total).toInt(), true)
        binding.bar4star.setProgressCompat(((rating.distribution.fourStar  * 100) / total).toInt(), true)
        binding.bar3star.setProgressCompat(((rating.distribution.threeStar * 100) / total).toInt(), true)
        binding.bar2star.setProgressCompat(((rating.distribution.twoStar   * 100) / total).toInt(), true)
        binding.bar1star.setProgressCompat(((rating.distribution.oneStar   * 100) / total).toInt(), true)

        binding.tv5starCount.text = rating.distribution.fiveStar.toString()
        binding.tv4starCount.text = rating.distribution.fourStar.toString()
        binding.tv3starCount.text = rating.distribution.threeStar.toString()
        binding.tv2starCount.text = rating.distribution.twoStar.toString()
        binding.tv1starCount.text = rating.distribution.oneStar.toString()

        // My score
        rating.myScore?.let { updateStarUI(it) }
    }

    // ===== STAR UI =====
    private fun updateStarUI(score: Int) {
        val starButtons = listOf(
            binding.btnStar1, binding.btnStar2, binding.btnStar3,
            binding.btnStar4, binding.btnStar5
        )
        starButtons.forEachIndexed { index, btn ->
            val filled = index < score
            btn.setImageResource(
                if (filled) R.drawable.ic_star else R.drawable.ic_star_border
            )
            btn.setColorFilter(
                getColor(if (filled) R.color.primary else R.color.on_surface_dim)
            )
        }
    }

    // ===== STATE =====
    private enum class State { LOADING, CONTENT, ERROR }

    private fun showState(state: State) {
        binding.layoutLoading.visibility  = if (state == State.LOADING) View.VISIBLE else View.GONE
        binding.scrollContent.visibility  = if (state == State.CONTENT) View.VISIBLE else View.GONE
        binding.bottomBar.visibility      = if (state == State.CONTENT) View.VISIBLE else View.GONE
        binding.layoutError.visibility    = if (state == State.ERROR)   View.VISIBLE else View.GONE
    }

    // ===== NAVIGATION =====
    private fun navigateToReader(chapter: ChapterSummaryDto) {
        val intent = Intent(this, com.example.library_mobile.ui.reading.ReadingActivity::class.java).apply {
            putExtra(com.example.library_mobile.ui.reading.ReadingActivity.EXTRA_CHAPTER_ID, chapter.id)
            putExtra(com.example.library_mobile.ui.reading.ReadingActivity.EXTRA_STORY_ID, storyId)
        }
        startActivity(intent)
    }

    // ===== HELPERS =====
    private fun buildStarString(avg: Double): String {
        val full  = avg.toInt()
        val half  = if (avg - full >= 0.5) 1 else 0
        val empty = 5 - full - half
        return "⭐".repeat(full) + (if (half > 0) "✨" else "") + "☆".repeat(empty)
    }

    private fun formatCount(count: Long): String = when {
        count >= 1_000_000 -> "${"%.1f".format(count / 1_000_000.0)}M"
        count >= 1_000     -> "${"%.1f".format(count / 1_000.0)}K"
        else               -> count.toString()
    }
}