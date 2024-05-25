package com.aldi.codestories.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.aldi.codestories.R
import com.aldi.codestories.data.local.UserPreference
import com.aldi.codestories.ui.main.MainActivity
import com.aldi.codestories.ui.WelcomeActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)


        userPreference = UserPreference.getInstance(dataStore)


        lifecycleScope.launch {
            val isLoggedIn = userPreference.isLoggedIn().first()
            if (isLoggedIn) {
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashScreenActivity, WelcomeActivity::class.java))
            }
            finish()
        }
    }
}
