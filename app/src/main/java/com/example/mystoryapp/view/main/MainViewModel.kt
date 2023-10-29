package com.example.mystoryapp.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mystoryapp.data.UserRepository
import com.example.mystoryapp.data.pref.UserModel
import com.example.mystoryapp.data.response.ListStoryItem
import kotlinx.coroutines.launch
import com.example.mystoryapp.data.Result

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

    fun getStories(token: String){
        val liveData = repository.getStories(token)
        _storyListItem.addSource(liveData) { result ->
            _storyListItem.value = result
        }
    }
}