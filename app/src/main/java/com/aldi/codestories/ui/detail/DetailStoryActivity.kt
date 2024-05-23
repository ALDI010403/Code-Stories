package com.aldi.codestories.ui.detail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aldi.codestories.R
import com.aldi.codestories.databinding.ActivityDetailStoryBinding
import com.aldi.codestories.response.ListStoryItem
import com.aldi.codestories.utils.withDateFormat
import com.bumptech.glide.Glide

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        val result = intent.getParcelableExtra<ListStoryItem>(EXTRA_RESULT)
        if (result != null) {
            setupToolbar(result)
            setupDetailStory(result)
            setupAccessibility()
        } else {
            showToast(getString(R.string.failed_to_load_data))
        }
    }

    private fun setupToolbar(item: ListStoryItem) {
        with(binding) {
            topAppBar.title = getString(R.string.s_story, item.name)

            topAppBar.setNavigationIcon(R.drawable.baseline_arrow_back_24)
            topAppBar.setNavigationOnClickListener {
                finish()
            }
        }
    }

    private fun setupAccessibility() {
        binding.apply {
            topAppBar.contentDescription = getString(R.string.navigation_and_actions)
            storyImage.contentDescription = getString(R.string.story_image)
            storyName.contentDescription = getString(R.string.story_name)
            storyDate.contentDescription = getString(R.string.story_date)
            storyDescription.contentDescription = getString(R.string.story_description)
        }
    }

    private fun setupDetailStory(items: ListStoryItem) {
        binding.apply {
            storyName.text = items.name ?: getString(R.string.not_available)
            storyDate.text = items.createdAt?.withDateFormat() ?: getString(R.string.not_available)
            storyDescription.text = items.description ?: getString(R.string.not_available)
            Glide.with(this@DetailStoryActivity)
                .load(items.photoUrl)
                .into(storyImage)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_RESULT = "extra_result"
    }
}