package com.aldi.codestories.viewmodel.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.lifecycle.asLiveData
import com.aldi.codestories.data.local.pref.UserPreference
import com.aldi.codestories.repository.Result
import com.aldi.codestories.repository.StoryRepository
import com.aldi.codestories.response.ListStoryItem
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: StoryRepository,
    private val pref: UserPreference
) : ViewModel() {

    val stories: LiveData<Result<PagingData<ListStoryItem>>> by lazy {
        repository.getAllStories()
            .map { result ->
                when (result) {
                    is Result.Success -> Result.Success(result.data)
                    is Result.Error -> Result.Error(result.error)
                    is Result.Loading -> Result.Loading
                }
            }.asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            pref.logout()
        }
    }
}
