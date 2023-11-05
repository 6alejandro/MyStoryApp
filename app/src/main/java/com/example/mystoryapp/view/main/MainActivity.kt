package com.example.mystoryapp.view.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mystoryapp.R
import com.example.mystoryapp.ViewModelFactory
import com.example.mystoryapp.data.Result
import com.example.mystoryapp.databinding.ActivityMainBinding
import com.example.mystoryapp.view.LoadingStateAdapter
import com.example.mystoryapp.view.StoriesAdapter
import com.example.mystoryapp.view.add.AddStoryActivity
import com.example.mystoryapp.view.map.MapsActivity
import com.example.mystoryapp.view.welcome.WelcomeActivity

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
//    private lateinit var adapter: StoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this)
        binding.rvStories.layoutManager = layoutManager

        getSession()
        getData()
//        setRecyclerView()
        setButtonAdd()
    }

    private fun getData() {
        val adapter = StoriesAdapter()
        binding.rvStories.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )
        viewModel.storyList.observe(this) {
            adapter.submitData(lifecycle, it)
//            Log.d("paging", "data stories: $it")
        }
    }

    private fun getSession() {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
//            else {
//                viewModel.getStories(user.token)
//            }
        }
    }

    private fun setButtonAdd() {
        binding.btnAddStory.setOnClickListener{
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }
    }

//    private fun setRecyclerView() {
//        viewModel.storyListItem.observe(this) {
//            when (it) {
//                is Result.Loading -> {
//                    showLoading(true)
//                }
//
//                is Result.Error -> {
//                    showLoading(false)
//                }
//
//                is Result.Success -> {
//                    showLoading(false)
//                    adapter = StoriesAdapter(it.data)
//                    binding.rvStories.adapter = adapter
//                }
//            }
//        }
//    }

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