package com.example.mystoryapp.view.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mystoryapp.data.UserRepository
import com.example.mystoryapp.data.pref.UserModel
import com.example.mystoryapp.data.response.ListStoryItem
import kotlinx.coroutines.launch
import com.example.mystoryapp.data.Result
import kotlinx.coroutines.flow.collect

class MainViewModel(private val repository: UserRepository) : ViewModel() {
    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    private val _storyListItem = MediatorLiveData<Result<List<ListStoryItem>>>()
    val storyListItem: LiveData<Result<List<ListStoryItem>>> = _storyListItem

    val storyList: LiveData<PagingData<ListStoryItem>> =
        repository.getStories(getToken()).cachedIn(viewModelScope)

    private var token: String = "token default"

    init {
        viewModelScope.launch {
            repository.getSession().collect{ user ->
                token = user.token
                Log.d("paging viewModel", "token: $token")
            }
        }
    }

    private fun getToken(): String {
        var token = ""
        viewModelScope.launch {
            repository.getSession().collect(){ user ->
                token = user.token
            }
        }
        return token
    }
}