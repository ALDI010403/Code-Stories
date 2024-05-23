package com.aldi.codestories.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.aldi.codestories.R
import com.aldi.codestories.data.local.UserPreference
import com.aldi.codestories.ui.login.LoginActivity
import com.aldi.codestories.ui.main.MainActivity
import com.aldi.codestories.ui.register.RegisterActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class WelcomeActivity : AppCompatActivity() {

    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)
        supportActionBar?.hide()

        // Initialize UserPreference
        userPreference = UserPreference.getInstance(dataStore)

        // Check if the user is already logged in
        lifecycleScope.launch {
            val isLoggedIn = userPreference.isLoggedIn().first()
            if (isLoggedIn) {
                // Navigate to the main activity directly
                startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                finish()
            } else {
                setupUI()
            }
        }
    }

    private fun setupUI() {
        val btnLoginStart = findViewById<Button>(R.id.loginButton_start)
        btnLoginStart.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        val btnRegistStart = findViewById<TextView>(R.id.registerButton_start)
        btnRegistStart.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}
