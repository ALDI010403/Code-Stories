package com.aldi.codestories.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aldi.codestories.R
import com.aldi.codestories.ViewModelFactory
import com.aldi.codestories.data.local.pref.UserPreference
import com.aldi.codestories.databinding.ActivityLoginBinding
import com.aldi.codestories.repository.Result
import com.aldi.codestories.ui.main.MainActivity
import com.aldi.codestories.ui.register.RegisterActivity
import com.aldi.codestories.viewmodel.login.LoginViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userPreference: UserPreference
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(SESSION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference.getInstance(dataStore)
        lifecycleScope.launch {
            try {
                userPreference.isLoggedIn().collect { isLoggedIn ->
                    if (isLoggedIn == true) {
                        moveToMain()
                    } else {
                        showLoading(false)
                        setupAnimation()
                        setupTitle()
                        setupButton()
                        setupAction()
                        setupAccessibility()
                    }
                }
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error in collecting user login status", e)
            }
        }
    }

    private fun setupAction() {
        binding.loginButton.setOnClickListener { setupLogin() }
    }

    private fun setupAccessibility() {
        binding.apply {
            loginDescription.contentDescription = getString(R.string.description_of_login)
            emailEditTextLayout.contentDescription = getString(R.string.email_input_field)
            passwordEditTextLayout.contentDescription = getString(R.string.password_input_field)
            loginButton.contentDescription = getString(R.string.login_button)
            registerButton.contentDescription = getString(R.string.register_button)
        }
    }

    private fun setupAnimation() {
        val loginDescription = ObjectAnimator.ofFloat(binding.loginDescription, View.ALPHA, 1f).setDuration(300)
        val edEmail = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(300)
        val edPassword = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(300)
        val loginButton = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(300)
        val registerButton = ObjectAnimator.ofFloat(binding.registerButton, View.ALPHA, 1f).setDuration(300)

        AnimatorSet().apply {
            playSequentially( loginDescription, edEmail, edPassword, loginButton, registerButton)
            start()
        }
    }

    private fun setupTitle() {
        val linoleumBlue = ContextCompat.getColor(this, R.color.blue)

        val loginTitle = getString(R.string.login_title)
        val codestories = getString(R.string.app_name)

        val spannable = SpannableString("$loginTitle $codestories")
        spannable.setSpan(
            ForegroundColorSpan(linoleumBlue),
            loginTitle.length + 1,
            loginTitle.length + 1 + codestories.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    private fun setupButton() {
        val linoleumBlue = ContextCompat.getColor(this, R.color.blue)

        val spannable = SpannableString(getString(R.string.login_to_signup, getString(R.string.signup_now)))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
        }

        val boldSpan = StyleSpan(Typeface.BOLD)
        spannable.setSpan(
            boldSpan,
            spannable.indexOf(getString(R.string.signup_now)),
            spannable.indexOf(getString(R.string.signup_now)) + getString(R.string.signup_now).length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            clickableSpan,
            spannable.indexOf(getString(R.string.signup_now)),
            spannable.indexOf(getString(R.string.signup_now)) + getString(R.string.signup_now).length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            ForegroundColorSpan(linoleumBlue),
            spannable.indexOf(getString(R.string.signup_now)),
            spannable.indexOf(getString(R.string.signup_now)) + getString(R.string.signup_now).length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.registerButton.text = spannable
        binding.registerButton.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupLogin() {
        val loginViewModel = obtainViewModel(this@LoginActivity)

        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        when {
            email.isEmpty() -> {
                binding.emailEditText.error = getString(R.string.empty_email)
            }
            password.isEmpty() -> {
                binding.passwordEditText.error = getString(R.string.empty_password)
            }
            else -> {
                loginViewModel.login(email, password).observe(this@LoginActivity) {
                    if (it != null) {
                        when (it) {
                            is Result.Loading -> {
                                showLoading(true)
                            }
                            is Result.Success -> {
                                showLoading(false)

                                val response = it.data

                                loginViewModel.saveLoginState(response.token.toString())
                                showToast(getString(R.string.successfully_logged_in))

                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            }
                            is Result.Error -> {
                                showLoading(false)
                                showToast(it.error)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun moveToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun obtainViewModel(activity: AppCompatActivity): LoginViewModel {
        val factory = ViewModelFactory.getInstance(
            activity.application,
            UserPreference.getInstance(dataStore)
        )
        return ViewModelProvider(activity, factory)[LoginViewModel::class.java]
    }

    companion object {
        const val SESSION = "session"
    }
}
