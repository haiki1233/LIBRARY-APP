package com.example.library_mobile.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.library_mobile.data.remote.dto.ChapterSummaryDto
import com.example.library_mobile.databinding.ItemChapterBinding

class ChapterListAdapter(
    private val onClick: (ChapterSummaryDto) -> Unit
) : ListAdapter<ChapterSummaryDto, ChapterListAdapter.ViewHolder>(DiffCallback()) {

    // Chapter đã đọc (highlight màu khác)
    private var lastReadChapterId: Long? = null

    fun setLastReadChapter(chapterId: Long?) {
        lastReadChapterId = chapterId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChapterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemChapterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chapter: ChapterSummaryDto) {
            val isLastRead = chapter.id == lastReadChapterId

            binding.tvChapterNumber.text = "Chương ${chapter.chapterNumber}"
            binding.tvChapterTitle.text  = chapter.title
            binding.tvCreatedAt.text     = formatDate(chapter.createdAt)

            // Highlight chapter đang đọc dở
            if (isLastRead) {
                binding.tvChapterNumber.setTextColor(
                    binding.root.context.getColor(com.example.library_mobile.R.color.primary)
                )
                binding.tvContinueTag.visibility = android.view.View.VISIBLE
            } else {
                binding.tvChapterNumber.setTextColor(
                    binding.root.context.getColor(com.example.library_mobile.R.color.on_surface)
                )
                binding.tvContinueTag.visibility = android.view.View.GONE
            }

            binding.root.setOnClickListener { onClick(chapter) }
        }

        private fun formatDate(dateStr: String?): String {
            if (dateStr.isNullOrBlank()) return ""
            return try {
                // Định dạng: "2024-01-15T10:30:00" → "15/01/2024"
                val parts = dateStr.substring(0, 10).split("-")
                "${parts[2]}/${parts[1]}/${parts[0]}"
            } catch (e: Exception) { "" }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChapterSummaryDto>() {
        override fun areItemsTheSame(a: ChapterSummaryDto, b: ChapterSummaryDto) = a.id == b.id
        override fun areContentsTheSame(a: ChapterSummaryDto, b: ChapterSummaryDto) = a == b
    }
}