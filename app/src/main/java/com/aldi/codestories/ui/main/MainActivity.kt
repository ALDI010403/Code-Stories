package com.aldi.codestories.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aldi.codestories.R
import com.aldi.codestories.ViewModelFactory
import com.aldi.codestories.adapter.StoryAdapter
import com.aldi.codestories.data.local.UserPreference
import com.aldi.codestories.databinding.ActivityMainBinding
import com.aldi.codestories.ui.addstory.AddStoryActivity
import com.aldi.codestories.ui.detail.DetailStoryActivity
import com.aldi.codestories.ui.login.LoginActivity
import com.aldi.codestories.viewmodel.main.MainViewModel
import com.aldi.codestories.repository.Result
import com.aldi.codestories.response.ListStoryItem
import com.aldi.codestories.ui.setting.SettingActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var storyAdapter: StoryAdapter

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SESSION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = obtainViewModel(this)
        storyAdapter = StoryAdapter()

        setupUI()
        observeStories()
    }

    private fun setupUI() {
        setupToolbar()
        setupRecyclerView()
        setupFab()
        setupAccessibility()
    }

    private fun setupToolbar() {
        binding.topAppBar.apply {
            title = getString(R.string.app_name)
            inflateMenu(R.menu.menu)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_settings -> {
                        moveToSetting()
                        true
                    }
                    R.id.menu_logout -> {
                        logout()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = storyAdapter
        }

        storyAdapter.setOnItemClickCallback(object : StoryAdapter.OnItemClickCallback {
            override fun onItemClicked(item: ListStoryItem?) {
                moveToDetailStory(item)
            }
        })
    }

    private fun setupFab() {
        binding.fabButton.setOnClickListener { moveToAddNewStory() }
    }

    private fun setupAccessibility() {
        binding.apply {
            topAppBar.contentDescription = getString(R.string.navigation_and_actions)
            rvStories.contentDescription = getString(R.string.list_of_stories)
            fabButton.contentDescription = getString(R.string.upload_new_story)
            storyNotAvailable.contentDescription = getString(R.string.no_stories_available)
        }
    }

    private fun observeStories() {
        mainViewModel.getAllStories().observe(this) { result ->
            when (result) {
                is Result.Loading -> showLoading(true)
                is Result.Success -> handleSuccess(result.data)
                is Result.Error -> handleError(result.error)
            }
        }
    }

    private fun handleSuccess(stories: List<ListStoryItem>) {
        showLoading(false)
        if (stories.isEmpty()) {
            binding.rvStories.visibility = View.GONE
            binding.storyNotAvailable.visibility = View.VISIBLE
        } else {
            binding.rvStories.visibility = View.VISIBLE
            binding.storyNotAvailable.visibility = View.GONE
            storyAdapter.submitList(stories)
        }
    }

    private fun handleError(error: String) {
        showLoading(false)
        showToast(error)
    }

    private fun moveToDetailStory(item: ListStoryItem?) {
        val intent = Intent(this, DetailStoryActivity::class.java).apply {
            putExtra(DetailStoryActivity.EXTRA_RESULT, item)
        }
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle())
    }

    private fun moveToAddNewStory() {
        startActivity(Intent(this, AddStoryActivity::class.java))
    }

    private fun moveToSetting() {
        startActivity(Intent(this, SettingActivity::class.java))
    }

    private fun logout() {
        mainViewModel.logout()
        showToast(getString(R.string.successfully_logged_out))
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun obtainViewModel(activity: AppCompatActivity): MainViewModel {
        val factory = ViewModelFactory.getInstance(
            activity.application, UserPreference.getInstance(dataStore)
        )
        return ViewModelProvider(activity, factory)[MainViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()
        observeStories()
    }

    companion object {
        private const val SESSION = "session"
    }
}
