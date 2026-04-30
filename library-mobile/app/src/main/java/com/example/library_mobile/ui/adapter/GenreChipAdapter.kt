package com.example.library_mobile.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.library_mobile.data.remote.dto.GenreDto
import com.example.library_mobile.databinding.ItemGenreChipBinding

class GenreChipAdapter(
    private val onSelect: (GenreDto?) -> Unit   // null = "Tất cả"
) : ListAdapter<GenreDto, GenreChipAdapter.ViewHolder>(DiffCallback()) {

    // Track chip đang được chọn
    private var selectedId: Long? = null

    // Tên thể loại đặc biệt - "Tất cả" có id = -1
    private val allItem = GenreDto(id = -1L, name = "Tất cả")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGenreChipBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // position 0 luôn là "Tất cả"
        val item = if (position == 0) allItem else getItem(position - 1)
        holder.bind(item, item.id == (selectedId ?: -1L))
    }

    override fun getItemCount() = super.getItemCount() + 1  // +1 cho "Tất cả"

    fun setSelected(genreId: Long?) {
        selectedId = genreId
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemGenreChipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(genre: GenreDto, isSelected: Boolean) {
            binding.chip.text      = genre.name
            binding.chip.isChecked = isSelected

            binding.chip.setOnClickListener {
                val prev = selectedId
                selectedId = genre.id

                // Deselect "Tất cả" → onSelect(null)
                if (genre.id == -1L) {
                    selectedId = -1L
                    onSelect(null)
                } else {
                    onSelect(genre)
                }

                // Refresh chỉ 2 item thay đổi
                notifyDataSetChanged()
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<GenreDto>() {
        override fun areItemsTheSame(a: GenreDto, b: GenreDto) = a.id == b.id
        override fun areContentsTheSame(a: GenreDto, b: GenreDto) = a == b
    }
}