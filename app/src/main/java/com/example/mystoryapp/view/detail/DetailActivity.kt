package com.example.mystoryapp.view.detail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.databinding.ActivityDetailBinding
import com.example.mystoryapp.view.add.AddStoryActivity
import com.example.mystoryapp.view.map.MapsActivity

class DetailActivity : AppCompatActivity() {
    private lateinit var listStoryItem: ListStoryItem
    private lateinit var binding: ActivityDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listStoryItem = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("listStoryItem", ListStoryItem::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("listStoryItem")!!
        }

        binding.tvTitle.text = listStoryItem.name
        binding.tvDescription.text = listStoryItem.description

        Glide
            .with(this)
            .load(listStoryItem.photoUrl)
            .fitCenter()
            .into(binding.ivStory)
    }
}