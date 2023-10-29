package com.example.mystoryapp.view.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.example.mystoryapp.data.UserRepository
import com.example.mystoryapp.data.response.RegisterResponse
import com.example.mystoryapp.data.Result

class SignUpViewModel(private val repository: UserRepository): ViewModel() {

    private val _registerResponse = MediatorLiveData<Result<RegisterResponse>>()
    val registerResponse: LiveData<Result<RegisterResponse>> = _registerResponse

    fun register(name: String, email: String, password: String){
        val liveData = repository.register(name, email, password)
        _registerResponse.addSource(liveData){ result ->
            _registerResponse.value = result
        }
    }
}