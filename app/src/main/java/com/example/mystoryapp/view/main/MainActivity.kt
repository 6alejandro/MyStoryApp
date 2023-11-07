package com.example.mystoryapp.view.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mystoryapp.R
import com.example.mystoryapp.ViewModelFactory
import com.example.mystoryapp.databinding.ActivityMainBinding
import com.example.mystoryapp.view.StoriesAdapter
import com.example.mystoryapp.view.add.AddStoryActivity
import com.example.mystoryapp.view.map.MapsActivity
import com.example.mystoryapp.view.welcome.WelcomeActivity

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: StoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showLoading(true)

        val layoutManager = LinearLayoutManager(this)
        binding.rvStories.layoutManager = layoutManager

        getSession()
        setButtonAdd()
    }

    private fun getSession() {
        viewModel.getSession().observe(this) { user ->
            val token = user.token
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
            adapter = StoriesAdapter()
            binding.rvStories.adapter = adapter
            viewModel.getStories(token).observe(this) {
                adapter.submitData(lifecycle, it)
            }
            showLoading(false)
        }
    }

    private fun setButtonAdd() {
        binding.btnAddStory.setOnClickListener{
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.rvStories.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_item, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.logout -> viewModel.logout()
            R.id.map -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}