package com.example.library_mobile.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.library_mobile.R
import com.example.library_mobile.data.remote.dto.FavoriteDto
import com.example.library_mobile.databinding.ItemFavoriteBinding

class FavoriteAdapter(
    private val onClick: (FavoriteDto) -> Unit,
    private val onRemove: (FavoriteDto) -> Unit
) : ListAdapter<FavoriteDto, FavoriteAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFavoriteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemFavoriteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FavoriteDto) {
            // Cover image
            Glide.with(binding.root)
                .load(item.storyCoverImage)
                .placeholder(R.drawable.placeholder_cover)
                .error(R.drawable.placeholder_cover)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.ivCover)

            binding.tvTitle.text    = item.storyTitle
            binding.tvAuthor.text   = item.storyAuthor ?: "Đang cập nhật"
            binding.tvChapters.text = "${item.totalChapters} chương"
            binding.tvRating.text   = "⭐ ${"%.1f".format(item.avgRating)}"
            binding.tvGenres.text   = item.genres.take(2).joinToString(" • ") { it.name }

            // Status badge
            val isCompleted = item.storyStatus == "COMPLETED"
            binding.tvStatus.text = if (isCompleted) "Full" else "Đang ra"
            binding.tvStatus.setBackgroundResource(
                if (isCompleted) R.drawable.bg_badge_completed
                else R.drawable.bg_badge_ongoing
            )

            // Thời gian lưu
            binding.tvSavedAt.text = "Đã lưu: ${formatDate(item.savedAt)}"

            // Cập nhật lần cuối
            binding.tvUpdatedAt.text = "Cập nhật: ${formatDate(item.storyUpdatedAt)}"

            binding.root.setOnClickListener { onClick(item) }
            binding.btnRemove.setOnClickListener { onRemove(item) }
        }

        private fun formatDate(dateStr: String?): String {
            if (dateStr.isNullOrBlank()) return "N/A"
            return try {
                val parts = dateStr.substring(0, 10).split("-")
                "${parts[2]}/${parts[1]}/${parts[0]}"
            } catch (e: Exception) { "N/A" }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<FavoriteDto>() {
        override fun areItemsTheSame(a: FavoriteDto, b: FavoriteDto) =
            a.favoriteId == b.favoriteId
        override fun areContentsTheSame(a: FavoriteDto, b: FavoriteDto) = a == b
    }

    // ===== Swipe to delete helper =====
    fun attachSwipeToDelete(
        recyclerView: RecyclerView,
        onSwiped: (FavoriteDto) -> Unit
    ) {
        val callback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT
        ) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_ID.toInt()) {
                    onSwiped(getItem(position))
                }
            }

            // Vẽ background đỏ + icon thùng rác khi swipe
            override fun onChildDraw(
                c: android.graphics.Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val paint    = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#FF5252")
                }
                val iconMargin = 32

                if (dX < 0) {
                    // Red background
                    c.drawRect(
                        itemView.right + dX, itemView.top.toFloat(),
                        itemView.right.toFloat(), itemView.bottom.toFloat(),
                        paint
                    )
                    // Trash icon
                    val icon = androidx.core.content.ContextCompat.getDrawable(
                        recyclerView.context, R.drawable.ic_delete
                    )
                    icon?.let {
                        it.setTint(android.graphics.Color.WHITE)
                        val iconTop    = itemView.top + (itemView.height - it.intrinsicHeight) / 2
                        val iconRight  = itemView.right - iconMargin
                        val iconLeft   = iconRight - it.intrinsicWidth
                        it.setBounds(iconLeft, iconTop, iconRight, iconTop + it.intrinsicHeight)
                        it.draw(c)
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }
}