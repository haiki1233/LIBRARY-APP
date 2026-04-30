package com.example.library_mobile.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.library_mobile.R
import com.example.library_mobile.data.remote.dto.StoryCardDto
import com.example.library_mobile.databinding.ItemStoryGridBinding

class StoryGridAdapter(
    private val onClick: (StoryCardDto) -> Unit
) : ListAdapter<StoryCardDto, StoryGridAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStoryGridBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemStoryGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(story: StoryCardDto) {
            binding.tvTitle.text   = story.title
            binding.tvAuthor.text  = story.author ?: "Đang cập nhật"
            binding.tvChapter.text = "${story.totalChapters} ch."

            // Genre tag - chỉ hiện tag đầu tiên
            val firstGenre = story.genres.firstOrNull()
            if (firstGenre != null) {
                binding.tvGenre.text = firstGenre.name
                binding.tvGenre.visibility = android.view.View.VISIBLE
            } else {
                binding.tvGenre.visibility = android.view.View.GONE
            }

            // Status badge
            binding.tvStatus.text = if (story.status == "COMPLETED") "Full" else "Đang ra"
            binding.tvStatus.setBackgroundResource(
                if (story.status == "COMPLETED") R.drawable.bg_badge_completed
                else R.drawable.bg_badge_ongoing
            )

            Glide.with(binding.root)
                .load(story.coverImage)
                .placeholder(R.drawable.placeholder_cover)
                .error(R.drawable.placeholder_cover)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.ivCover)

            binding.root.setOnClickListener { onClick(story) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<StoryCardDto>() {
        override fun areItemsTheSame(a: StoryCardDto, b: StoryCardDto) = a.id == b.id
        override fun areContentsTheSame(a: StoryCardDto, b: StoryCardDto) = a == b
    }
}