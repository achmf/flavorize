package com.example.flavorize.ui.fragments.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flavorize.databinding.ItemHomeContentBinding
import com.example.flavorize.ui.fragments.home.data.HomeContent

class HomeAdapter(
    private var contentList: List<HomeContent>
) : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    inner class HomeViewHolder(private val binding: ItemHomeContentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(content: HomeContent) {
            binding.titleTextView.text = content.title
            binding.descriptionTextView.text = content.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val binding = ItemHomeContentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HomeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.bind(contentList[position])
    }

    override fun getItemCount(): Int = contentList.size

    fun updateContent(newContent: List<HomeContent>) {
        contentList = newContent
        notifyDataSetChanged()
    }
}
