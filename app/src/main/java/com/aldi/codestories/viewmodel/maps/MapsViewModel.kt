package com.aldi.codestories.viewmodel.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.aldi.codestories.repository.Result
import com.aldi.codestories.repository.StoryRepository
import com.aldi.codestories.response.ListStoryItem

class MapsViewModel(
    private val repository: StoryRepository
) : ViewModel() {

    fun getStoriesWithLocation(): LiveData<Result<List<ListStoryItem>>> {
        return repository.getStoriesWithLocation()
    }

}