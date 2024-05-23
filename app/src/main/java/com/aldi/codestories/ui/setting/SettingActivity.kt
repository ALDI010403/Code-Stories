package com.aldi.codestories.ui.setting

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.aldi.codestories.R
import com.aldi.codestories.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupAction()
        setupAccessibility()
    }

    private fun setupToolbar() {
        with(binding) {
            topAppBar.title = getString(R.string.settings)

            topAppBar.setNavigationIcon(R.drawable.baseline_arrow_back_24)
            topAppBar.setNavigationOnClickListener {
                finish()
            }
        }
    }

    private fun setupAction() {
        binding.languageButton.setOnClickListener { moveToLanguage() }
    }

    private fun setupAccessibility() {
        binding.apply {
            topAppBar.contentDescription = getString(R.string.navigation_and_actions)
            languageButton.contentDescription = getString(R.string.change_language)
        }
    }

    private fun moveToLanguage() {
        startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
    }
}