package com.example.mystoryapp.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.data.response.StoriesResponse
import com.example.mystoryapp.databinding.ItemRowBinding

class StoriesAdapter(private val listStories: List<ListStoryItem>
):
    RecyclerView.Adapter<StoriesAdapter.ListViewHolder>() {
        inner class ListViewHolder(private val binding: ItemRowBinding):
            RecyclerView.ViewHolder(binding.root){
            fun bind(listStoryItem: ListStoryItem){
                binding.tvTitle.text = listStoryItem.name
                binding.tvDescription.text = listStoryItem.description

                Glide
                    .with(itemView.context)
                    .load(listStoryItem.photoUrl)
                    .fitCenter()
                    .into(binding.ivStory)
            }
        }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoriesAdapter.ListViewHolder {
        return ListViewHolder(
            ItemRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: StoriesAdapter.ListViewHolder, position: Int) {
        val item  = listStories[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return listStories.size
    }
}