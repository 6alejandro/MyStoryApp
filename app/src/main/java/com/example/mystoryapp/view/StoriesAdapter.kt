package com.example.mystoryapp.view

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mystoryapp.R
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.databinding.ItemRowBinding
import com.example.mystoryapp.view.detail.DetailActivity

class StoriesAdapter: PagingDataAdapter<ListStoryItem, StoriesAdapter.ListViewHolder>(DIFF_CALLBACK){
        class ListViewHolder(private val binding: ItemRowBinding):
            RecyclerView.ViewHolder(binding.root){
            private var ivStory: ImageView = itemView.findViewById(R.id.iv_story)
            private var tvName: TextView = itemView.findViewById(R.id.tv_title)
            fun bind(listStoryItem: ListStoryItem){
                binding.tvTitle.text = listStoryItem.name

                Glide
                    .with(itemView.context)
                    .load(listStoryItem.photoUrl)
                    .fitCenter()
                    .into(binding.ivStory)

                binding.itemRow.setOnClickListener {
                    val intent = Intent(itemView.context, DetailActivity::class.java)
                    intent.putExtra("listStoryItem", listStoryItem)

                    val optionsCompat: ActivityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            itemView.context as Activity,
                            Pair(ivStory, "photo"),
                            Pair(tvName, "name")
                        )
                    itemView.context.startActivity(intent, optionsCompat.toBundle())
                }
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
//        val item  = listStories[position]
//        holder.bind(item)
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
        }

//    override fun getItemCount(): Int {
//        return listStories.size
//    }

    companion object {
        private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areContentsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}