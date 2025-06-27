package com.example.tracsactiontracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.BuildConfig
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var signUpText: TextView
    private lateinit var forgotPasswordText: TextView
    private lateinit var googleSignInButton: MaterialButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        emailLayout = findViewById(R.id.email_layout)
        passwordLayout = findViewById(R.id.password_layout)
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.btn_signin)
        signUpText = findViewById(R.id.sign_up)
        forgotPasswordText = findViewById(R.id.forgot_password)
        googleSignInButton = findViewById(R.id.google_sign_in_button)

        // Simple dummy login - bypasses validation in debug builds
        if (BuildConfig.DEBUG) {
            loginButton.setOnClickListener {
                navigateToMainActivity()
            }
        } else {
            // Basic validation for non-debug builds
            loginButton.setOnClickListener {
                val email = emailInput.text.toString().trim()
                val password = passwordInput.text.toString().trim()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    navigateToMainActivity()
                } else {
                    Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Other dummy interactions
        signUpText.setOnClickListener {
            Toast.makeText(this, "Sign up screen would open here", Toast.LENGTH_SHORT).show()
        }

        forgotPasswordText.setOnClickListener {
            Toast.makeText(this, "Password reset would open here", Toast.LENGTH_SHORT).show()
        }

        googleSignInButton.setOnClickListener {
            Toast.makeText(this, "Google Sign In would happen here", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Finish LoginActivity so user can't go back
    }

    override fun onBackPressed() {
        // Prevent going back to onboarding
        Toast.makeText(this, "Please complete the login process", Toast.LENGTH_SHORT).show()
    }
}