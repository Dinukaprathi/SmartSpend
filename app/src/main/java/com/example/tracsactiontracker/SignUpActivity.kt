package com.example.tracsactiontracker

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var nameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var nameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var signUpButton: MaterialButton
    private lateinit var loginText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        nameLayout = findViewById(R.id.name_layout)
        emailLayout = findViewById(R.id.email_layout)
        passwordLayout = findViewById(R.id.password_layout)
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout)
        nameInput = findViewById(R.id.name_input)
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        confirmPasswordInput = findViewById(R.id.confirm_password_input)
        signUpButton = findViewById(R.id.sign_up_button)
        loginText = findViewById(R.id.login)

        // Set click listeners
        signUpButton.setOnClickListener {
            val name = nameInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            if (validateInput(name, email, password, confirmPassword)) {
                signUpUser(name, email, password)
            }
        }

        loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInput(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            nameLayout.error = "Name is required"
            isValid = false
        } else {
            nameLayout.error = null
        }

        if (email.isEmpty()) {
            emailLayout.error = "Email is required"
            isValid = false
        } else {
            emailLayout.error = null
        }

        if (password.isEmpty()) {
            passwordLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordLayout.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            passwordLayout.error = null
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordLayout.error = "Passwords do not match"
            isValid = false
        } else {
            confirmPasswordLayout.error = null
        }

        return isValid
    }

    private fun signUpUser(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    // Sign up success, save user data to Firestore
                    val user = auth.currentUser
                    user?.let {
                        val userData = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "createdAt" to System.currentTimeMillis()
                        )

                        db.collection("users")
                            .document(it.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                // Navigate to MainActivity
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error saving user data: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    // If sign up fails, display a message to the user.
                    Toast.makeText(
                        this,
                        "Sign up failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}