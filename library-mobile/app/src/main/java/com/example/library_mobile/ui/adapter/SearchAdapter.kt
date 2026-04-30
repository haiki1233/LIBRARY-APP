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
import com.example.library_mobile.databinding.ItemSearchResultBinding

class SearchAdapter(
    private val onClick: (StoryCardDto) -> Unit
) : ListAdapter<StoryCardDto, SearchAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchResultBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemSearchResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(story: StoryCardDto) {
            binding.tvTitle.text    = story.title
            binding.tvAuthor.text   = story.author ?: "Đang cập nhật"
            binding.tvChapters.text = "${story.totalChapters} chương"
            binding.tvRating.text   = "⭐ ${"%.1f".format(story.avgRating)}"
            binding.tvStatus.text   = if (story.status == "COMPLETED") "Full" else "Đang ra"
            binding.tvGenres.text   = story.genres.take(2).joinToString(" • ") { it.name }

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