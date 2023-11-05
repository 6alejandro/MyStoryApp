package com.example.mystoryapp.view.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.mystoryapp.data.UserRepository
import com.example.mystoryapp.data.pref.UserModel
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.data.Result

class MapsViewModel(private val repository: UserRepository): ViewModel() {

    private val _storyListWithLocation = MediatorLiveData<Result<List<ListStoryItem>>>()
    var storyListWithLocation: LiveData<Result<List<ListStoryItem>>> = _storyListWithLocation

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }
    fun getStoriesWithLocation(token: String) {
        val liveData = repository.getStoriesWithLocation(token)
        _storyListWithLocation.addSource(liveData) { result ->
            _storyListWithLocation.value = result
        }
    }
}