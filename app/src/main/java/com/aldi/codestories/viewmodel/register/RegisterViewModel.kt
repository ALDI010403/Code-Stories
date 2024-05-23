package com.aldi.codestories.viewmodel.register

import androidx.lifecycle.ViewModel
import com.aldi.codestories.repository.StoryRepository

class RegisterViewModel(private val repository: StoryRepository): ViewModel() {

    fun register(name: String, email: String, password: String) = repository.register(name, email, password)

}