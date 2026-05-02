package com.example.library_mobile.ui.reading

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.library_mobile.R
import com.example.library_mobile.data.local.ReadingPreferences
import com.example.library_mobile.data.repository.ReadingRepository
import com.example.library_mobile.databinding.ActivityReadingBinding
import com.example.library_mobile.utils.AppModule
import com.example.library_mobile.utils.Result
import com.example.library_mobile.utils.hide
import com.example.library_mobile.utils.show

class ReadingActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CHAPTER_ID = "chapter_id"
        const val EXTRA_STORY_ID   = "story_id"
    }

    private lateinit var binding: ActivityReadingBinding

    private val viewModel: ReadingViewModel by viewModels {
        val prefs = ReadingPreferences(this)
        val repo  = ReadingRepository(AppModule.provideReadingApiService(this))
        ReadingViewModel.Factory(repo, prefs)
    }

    private lateinit var imageAdapter: ChapterImageAdapter

    private var chapterId: Long = -1L
    private var currentChapterData: com.example.library_mobile.data.remote.dto.ChapterDetailDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Full screen - ẩn status bar khi đọc
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding = ActivityReadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chapterId = intent.getLongExtra(EXTRA_CHAPTER_ID, -1L)
        if (chapterId == -1L) { finish(); return }

        setupImageList()
        setupScrollTracking()
        setupClickListeners()
        observeViewModel()

        viewModel.loadChapter(chapterId)
    }

    // ===== SETUP =====
    private fun setupImageList() {
        imageAdapter = ChapterImageAdapter(onTap = { viewModel.toggleToolbar() })

        binding.rvImages.apply {
            adapter = imageAdapter
            layoutManager = LinearLayoutManager(this@ReadingActivity)
            setHasFixedSize(false)
        }
    }

    private fun setupScrollTracking() {
        // Track scroll cho image mode
        binding.rvImages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val lm = rv.layoutManager as LinearLayoutManager
                val totalItems   = lm.itemCount
                val firstVisible = lm.findFirstVisibleItemPosition()
                if (totalItems == 0) return

                val scrollPercent = ((firstVisible + 1) * 100) / totalItems
                viewModel.onScrollPositionChanged(scrollPercent)
                updateProgress(firstVisible + 1, totalItems)
            }
        })

        // Track scroll cho text mode
        binding.scrollText.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val totalHeight = binding.tvChapterContent.height
            if (totalHeight == 0) return@setOnScrollChangeListener
            val scrollPercent = (scrollY * 100) / totalHeight
            viewModel.onScrollPositionChanged(scrollPercent.coerceIn(0, 100))
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            viewModel.saveHistoryNow()
            finish()
        }

        binding.btnSettings.setOnClickListener { viewModel.toggleSettings() }

        // Tap trên text mode để toggle toolbar
        binding.scrollText.setOnClickListener { viewModel.toggleToolbar() }

        // Dim overlay đóng settings
        binding.dimOverlay.setOnClickListener { viewModel.hideSettings() }

        // Chương trước
        binding.btnPrevChapter.setOnClickListener {
            currentChapterData?.navigation?.prevChapterId?.let { prevId ->
                viewModel.saveHistoryNow()
                chapterId = prevId
                viewModel.loadChapter(prevId)
                scrollToTop()
            }
        }

        // Chương sau
        binding.btnNextChapter.setOnClickListener {
            currentChapterData?.navigation?.nextChapterId?.let { nextId ->
                viewModel.saveHistoryNow()
                chapterId = nextId
                viewModel.loadChapter(nextId)
                scrollToTop()
            }
        }

        binding.btnRetry.setOnClickListener { viewModel.loadChapter(chapterId) }

        // ── SETTINGS PANEL ──
        binding.switchDarkMode.setOnCheckedChangeListener { _, _ -> viewModel.toggleDarkMode() }

        // Read mode toggle
        binding.btnModeImage.setOnClickListener {
            viewModel.setReadMode(ReadingPreferences.ReadMode.IMAGE)
        }
        binding.btnModeText.setOnClickListener {
            viewModel.setReadMode(ReadingPreferences.ReadMode.TEXT)
        }

        // Font size
        binding.btnFontIncrease.setOnClickListener { viewModel.increaseFontSize() }
        binding.btnFontDecrease.setOnClickListener { viewModel.decreaseFontSize() }

        // Line spacing
        binding.btnSpacingCompact.setOnClickListener { viewModel.setLineSpacing(1.2f) }
        binding.btnSpacingNormal.setOnClickListener  { viewModel.setLineSpacing(1.6f) }
        binding.btnSpacingWide.setOnClickListener    { viewModel.setLineSpacing(2.2f) }

        // Font family
        binding.btnFontDefault.setOnClickListener {
            viewModel.setFontFamily(ReadingPreferences.FontFamily.DEFAULT)
        }
        binding.btnFontSerif.setOnClickListener {
            viewModel.setFontFamily(ReadingPreferences.FontFamily.SERIF)
        }
        binding.btnFontMono.setOnClickListener {
            viewModel.setFontFamily(ReadingPreferences.FontFamily.MONOSPACE)
        }
    }

    // ===== OBSERVE =====
    private fun observeViewModel() {

        viewModel.chapterState.observe(this) { result ->
            when (result) {
                is Result.Loading -> showState(State.LOADING)

                is Result.Success -> {
                    currentChapterData = result.data
                    showState(State.CONTENT)
                    bindChapter(result.data)
                }

                is Result.Error -> {
                    showState(State.ERROR)
                    binding.tvErrorMsg.text = result.message
                }
            }
        }

        // Toolbar visibility với animation
        viewModel.isToolbarVisible.observe(this) { visible ->
            if (visible) {
                binding.toolbarTop.show()
                binding.toolbarBottom.show()
                binding.toolbarTop.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.slide_down_fade_in)
                )
                binding.toolbarBottom.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
                )
            } else {
                binding.toolbarTop.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_out)
                )
                binding.toolbarBottom.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.slide_down_fade_out)
                )
                binding.toolbarTop.hide()
                binding.toolbarBottom.hide()
            }
        }

        // Settings panel với slide animation
        viewModel.isSettingsVisible.observe(this) { visible ->
            if (visible) {
                binding.panelSettings.show()
                binding.dimOverlay.show()
                binding.panelSettings.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
                )
            } else {
                binding.panelSettings.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.slide_down_fade_out)
                )
                binding.panelSettings.hide()
                binding.dimOverlay.hide()
            }
        }

        // Settings changes → apply to UI
        viewModel.settings.observe(this) { settings ->
            applySettings(settings)
        }
    }

    // ===== BIND DATA =====
    private fun bindChapter(chapter: com.example.library_mobile.data.remote.dto.ChapterDetailDto) {
        binding.tvStoryTitle.text   = chapter.storyTitle
        binding.tvChapterTitle.text = getString(R.string.chapter_title_format, chapter.chapterNumber, chapter.title)

        // Images cho image mode
        imageAdapter.submitList(chapter.images)

        // Text placeholder (backend trả ảnh, không có text content)
        binding.tvChapterContent.text = StringBuilder().apply {
            append(getString(R.string.text_mode_placeholder_header, chapter.chapterNumber, chapter.title))
            append(getString(R.string.text_mode_placeholder_body))
        }.toString()

        // Navigation buttons
        val nav = chapter.navigation
        binding.btnPrevChapter.isEnabled = nav.prevChapterId != null
        binding.btnPrevChapter.alpha     = if (nav.prevChapterId != null) 1f else 0.4f
        binding.btnNextChapter.isEnabled = nav.nextChapterId != null
        binding.btnNextChapter.alpha     = if (nav.nextChapterId != null) 1f else 0.4f

        // Progress reset
        updateProgress(1, chapter.images.size.coerceAtLeast(1))
    }

    // ===== APPLY SETTINGS =====
    private fun applySettings(settings: ReadingSettings) {
        val isImageMode = settings.readMode == ReadingPreferences.ReadMode.IMAGE

        // Switch mode
        binding.rvImages.visibility   = if (isImageMode) View.VISIBLE else View.GONE
        binding.scrollText.visibility = if (isImageMode) View.GONE    else View.VISIBLE

        // Mode buttons highlight
        binding.btnModeImage.apply {
            setBackgroundResource(if (isImageMode) R.drawable.bg_mode_selected else android.R.color.transparent)
            setTextColor(getColor(if (isImageMode) R.color.on_background else R.color.on_surface_dim))
        }
        binding.btnModeText.apply {
            setBackgroundResource(if (!isImageMode) R.drawable.bg_mode_selected else android.R.color.transparent)
            setTextColor(getColor(if (!isImageMode) R.color.on_background else R.color.on_surface_dim))
        }

        // Font settings panel - chỉ hiện ở text mode
        binding.layoutFontSettings.visibility = if (isImageMode) View.GONE else View.VISIBLE

        // Apply text settings
        binding.tvChapterContent.textSize           = settings.fontSize
        binding.tvChapterContent.setLineSpacing(0f, settings.lineSpacing)
        binding.tvFontSize.text                     = settings.fontSize.toInt().toString()

        // Font family
        binding.tvChapterContent.typeface = when (settings.fontFamily) {
            ReadingPreferences.FontFamily.SERIF      -> Typeface.SERIF
            ReadingPreferences.FontFamily.MONOSPACE  -> Typeface.MONOSPACE
            else                                     -> Typeface.DEFAULT
        }

        // Dark / Light mode background + text color
        val bgColor   = if (settings.isDarkMode) getColor(R.color.reading_bg_dark)
        else getColor(R.color.reading_bg_light)
        val textColor = if (settings.isDarkMode) getColor(R.color.reading_text_dark)
        else getColor(R.color.reading_text_light)

        binding.coordinator.setBackgroundColor(bgColor)
        binding.scrollText.setBackgroundColor(bgColor)
        binding.tvChapterContent.setTextColor(textColor)
        binding.switchDarkMode.isChecked = settings.isDarkMode

        // Font buttons highlight
        val ff = settings.fontFamily
        updateFontButtonsUI(ff)
    }

    private fun updateFontButtonsUI(family: ReadingPreferences.FontFamily) {
        listOf(
            Pair(binding.btnFontDefault, ReadingPreferences.FontFamily.DEFAULT),
            Pair(binding.btnFontSerif,   ReadingPreferences.FontFamily.SERIF),
            Pair(binding.btnFontMono,    ReadingPreferences.FontFamily.MONOSPACE)
        ).forEach { (btn, f) ->
            val selected = f == family
            btn.setBackgroundResource(
                if (selected) R.drawable.bg_spacing_btn_selected else R.drawable.bg_spacing_btn
            )
            btn.setTextColor(
                getColor(if (selected) R.color.primary else R.color.on_surface_dim)
            )
        }
    }

    // ===== HELPERS =====
    private fun updateProgress(current: Int, total: Int) {
        binding.tvProgress.text = getString(R.string.progress_format, current, total)
        binding.progressReading.setProgressCompat(
            (current * 100) / total.coerceAtLeast(1), true
        )
    }

    private fun scrollToTop() {
        binding.rvImages.scrollToPosition(0)
        binding.scrollText.scrollTo(0, 0)
    }

    private enum class State { LOADING, CONTENT, ERROR }

    private fun showState(state: State) {
        binding.layoutLoading.visibility = if (state == State.LOADING) View.VISIBLE else View.GONE
        binding.layoutError.visibility   = if (state == State.ERROR)   View.VISIBLE else View.GONE
        // Content views (rvImages hoặc scrollText) được quản lý bởi applySettings()
        if (state == State.CONTENT) {
            viewModel.settings.value?.let { applySettings(it) }
        } else {
            binding.rvImages.hide()
            binding.scrollText.hide()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveHistoryNow()
    }
}