package com.example.tracsactiontracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.button.MaterialButton

class OnboardingActivity : AppCompatActivity() {

    private lateinit var buttonNext: MaterialButton
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        buttonNext = findViewById(R.id.buttonNext)
        
        // Set up Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        buttonNext.setOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.onboardingFragment1 -> {
                    navController.navigate(R.id.action_onboardingFragment1_to_onboardingFragment2)
                }
                R.id.onboardingFragment2 -> {
                    navController.navigate(R.id.action_onboardingFragment2_to_onboardingFragment3)
                }
                R.id.onboardingFragment3 -> {
                    // Navigate to LoginActivity
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish() // Finish OnboardingActivity so user can't go back
                }
            }
        }

        // Update button text based on current fragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            buttonNext.text = when (destination.id) {
                R.id.onboardingFragment3 -> getString(R.string.get_started)
                else -> getString(R.string.next)
            }
        }
    }

    override fun onBackPressed() {
        // If we're on the first fragment, let the system handle back press
        if (navController.currentDestination?.id == R.id.onboardingFragment1) {
            super.onBackPressed()
        } else {
            // Otherwise, navigate back in the onboarding flow
            navController.navigateUp()
        }
    }
} 