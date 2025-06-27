package com.example.tracsactiontracker

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.tracsactiontracker.onboarding.OnboardingFragment1
import com.example.tracsactiontracker.onboarding.OnboardingFragment2
import com.example.tracsactiontracker.onboarding.OnboardingFragment3

class OnboardingAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingFragment1()
            1 -> OnboardingFragment2()
            2 -> OnboardingFragment3()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
} 