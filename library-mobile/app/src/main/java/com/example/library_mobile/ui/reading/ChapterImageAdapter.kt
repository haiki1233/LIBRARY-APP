package com.example.library_mobile.ui.reading

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.library_mobile.R
import com.example.library_mobile.data.remote.dto.ChapterImageDto
import com.example.library_mobile.databinding.ItemChapterImageBinding

class ChapterImageAdapter(
    private val onTap: () -> Unit
) : ListAdapter<ChapterImageDto, ChapterImageAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChapterImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemChapterImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(image: ChapterImageDto) {
            // Loading placeholder
            binding.progressBar.visibility = android.view.View.VISIBLE

            Glide.with(binding.root)
                .load(image.imageUrl)
                .placeholder(R.drawable.placeholder_image_loading)
                .error(R.drawable.placeholder_image_error)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                    override fun onLoadFailed(
                        e: com.bumptech.glide.load.engine.GlideException?,
                        model: Any?, target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBar.visibility = android.view.View.GONE
                        binding.layoutError.visibility = android.view.View.VISIBLE
                        return false
                    }
                    override fun onResourceReady(
                        resource: android.graphics.drawable.Drawable,
                        model: Any, target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                        dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBar.visibility = android.view.View.GONE
                        binding.layoutError.visibility = android.view.View.GONE
                        return false
                    }
                })
                .into(binding.ivPage)

            // Tap to toggle toolbar
            binding.root.setOnClickListener { onTap() }

            // Retry khi load lỗi
            binding.btnRetryImage.setOnClickListener {
                binding.layoutError.visibility = android.view.View.GONE
                binding.progressBar.visibility = android.view.View.VISIBLE
                Glide.with(binding.root).load(image.imageUrl).into(binding.ivPage)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChapterImageDto>() {
        override fun areItemsTheSame(a: ChapterImageDto, b: ChapterImageDto) = a.id == b.id
        override fun areContentsTheSame(a: ChapterImageDto, b: ChapterImageDto) = a == b
    }
}