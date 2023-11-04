package com.example.mystoryapp.data

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import com.example.mystoryapp.data.api.ApiService
import com.example.mystoryapp.data.pref.UserModel
import com.example.mystoryapp.data.pref.UserPreference
import com.example.mystoryapp.data.response.AddResponse
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.data.response.LoginResponse
import com.example.mystoryapp.data.response.RegisterResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
){
    private suspend fun saveSession(user: UserModel) {
    userPreference.saveSession(user)
}

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    fun register(
        name: String,
        email: String,
        password: String
    ):LiveData<Result<RegisterResponse>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                val response = apiService.register(name, email, password)
                emit(Result.Success(response))
            } catch (e: Exception){
                emit(Result.Error(e.message.toString()))
            }
        }

    fun login(
        email: String,
        password: String
    ):LiveData<Result<LoginResponse>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                val response = apiService.login(email, password)
                val token = response.loginResult.token
                saveSession(UserModel(email, token))
                emit(Result.Success(response))
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
            }
        }

    fun getStories(token: String): LiveData<Result<List<ListStoryItem>>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                val response = apiService.getStories(("Bearer $token"))
                val stories = response.listStory
                emit(Result.Success(stories))
            } catch (e: Exception){
                emit(Result.Error(e.message.toString()))
            }

        }
    fun addStory(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        currentLocation: Location?
    ): LiveData<Result<AddResponse>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                val response =
                    if (currentLocation != null) {
                        apiService.postStories(
                            "Bearer $token", file, description,
                            currentLocation?.latitude.toString()
                                .toRequestBody("text/plain".toMediaType()),
                            currentLocation?.longitude.toString()
                                .toRequestBody("text/plain".toMediaType())
                        )
                    } else {
                        apiService.postStories("Bearer $token", file, description)
            }
                emit (Result.Success(response))
                } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
            }
        }

    fun getStoriesWithLocation(token: String): LiveData<Result<List<ListStoryItem>>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                val response = apiService.getStoriesWithLocation("Bearer $token")
                val storyList = response.listStory
                emit(Result.Success(storyList))
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
            }
        }
    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, apiService)
            }.also { instance = it }
    }
}