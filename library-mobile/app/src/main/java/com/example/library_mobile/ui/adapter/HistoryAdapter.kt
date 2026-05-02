package com.example.library_mobile.ui.adapter

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.library_mobile.R
import com.example.library_mobile.data.remote.dto.HistoryDto
import com.example.library_mobile.databinding.ItemHistoryBinding

class HistoryAdapter(
    private val onContinueRead: (HistoryDto) -> Unit,
    private val onStoryClick: (HistoryDto) -> Unit,
    private val onDelete: (HistoryDto) -> Unit
) : ListAdapter<HistoryDto, HistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HistoryDto) {
            // Cover
            Glide.with(binding.root)
                .load(item.storyCoverImage)
                .placeholder(R.drawable.placeholder_cover)
                .error(R.drawable.placeholder_cover)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.ivCover)

            binding.tvTitle.text        = item.storyTitle
            binding.tvAuthor.text       = item.storyAuthor ?: "Đang cập nhật"
            binding.tvLastChapter.text  = "Đang đọc: Chương ${item.chapterNumber}"
            binding.tvChapterTitle.text = item.chapterTitle
            binding.tvTotalChapters.text = "/ ${item.totalChapters} chương"
            binding.tvLastRead.text     = formatRelativeTime(item.lastReadAt)

            // Progress bar đọc đến đâu trong chapter
            binding.progressReading.setProgressCompat(item.scrollPosition, true)
            binding.tvScrollPercent.text = "${item.scrollPosition}%"

            // Status badge
            val isCompleted = item.storyStatus == "COMPLETED"
            binding.tvStatus.text = if (isCompleted) "Full" else "Đang ra"
            binding.tvStatus.setBackgroundResource(
                if (isCompleted) R.drawable.bg_badge_completed
                else R.drawable.bg_badge_ongoing
            )

            // Nút Đọc tiếp
            binding.btnContinue.setOnClickListener { onContinueRead(item) }

            // Tap vào card → vào story detail
            binding.root.setOnClickListener { onStoryClick(item) }

            // Nút xóa
            binding.btnDelete.setOnClickListener { onDelete(item) }
        }

        // Format thời gian tương đối: "2 giờ trước", "Hôm qua", ...
        private fun formatRelativeTime(dateStr: String?): String {
            if (dateStr.isNullOrBlank()) return ""
            return try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                val date = sdf.parse(dateStr) ?: return ""
                val now  = java.util.Date()
                val diffMs = now.time - date.time
                val diffMin  = diffMs / 60_000
                val diffHour = diffMs / 3_600_000
                val diffDay  = diffMs / 86_400_000

                when {
                    diffMin  < 1   -> "Vừa xong"
                    diffMin  < 60  -> "$diffMin phút trước"
                    diffHour < 24  -> "$diffHour giờ trước"
                    diffDay  == 1L -> "Hôm qua"
                    diffDay  < 7   -> "$diffDay ngày trước"
                    else -> {
                        val parts = dateStr.substring(0, 10).split("-")
                        "${parts[2]}/${parts[1]}/${parts[0]}"
                    }
                }
            } catch (e: Exception) { "" }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HistoryDto>() {
        override fun areItemsTheSame(a: HistoryDto, b: HistoryDto) = a.historyId == b.historyId
        override fun areContentsTheSame(a: HistoryDto, b: HistoryDto) = a == b
    }

    // ===== Swipe to delete =====
    fun attachSwipeToDelete(recyclerView: RecyclerView, onSwiped: (HistoryDto) -> Unit) {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                if (pos != RecyclerView.NO_ID.toInt()) onSwiped(getItem(pos))
            }

            override fun onChildDraw(
                c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val item   = vh.itemView
                val paint  = Paint().apply { color = Color.parseColor("#FF5252") }
                val margin = 36

                if (dX < 0) {
                    c.drawRect(
                        item.right + dX, item.top.toFloat(),
                        item.right.toFloat(), item.bottom.toFloat(), paint
                    )
                    val icon = ContextCompat.getDrawable(rv.context, R.drawable.ic_delete)
                    icon?.let {
                        it.setTint(Color.WHITE)
                        val iconTop   = item.top + (item.height - it.intrinsicHeight) / 2
                        val iconRight = item.right - margin
                        val iconLeft  = iconRight - it.intrinsicWidth
                        it.setBounds(iconLeft, iconTop, iconRight, iconTop + it.intrinsicHeight)
                        it.draw(c)
                    }
                }
                super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }
}