package com.aldi.codestories.viewmodel.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldi.codestories.data.local.UserPreference
import com.aldi.codestories.repository.Result
import com.aldi.codestories.repository.StoryRepository
import com.aldi.codestories.response.ListStoryItem
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: StoryRepository,
    private val pref: UserPreference
) : ViewModel() {

    fun getAllStories(): LiveData<Result<List<ListStoryItem>>> {
        return repository.getAllStories()
    }

    fun logout() {
        viewModelScope.launch {
            pref.logout()
        }
    }
}