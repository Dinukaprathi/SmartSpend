package com.example.tracsactiontracker.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import androidx.fragment.app.Fragment
import com.example.tracsactiontracker.R

class OnboardingFragment3 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding_3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val image = view.findViewById<ImageView>(R.id.onboarding_image)
        val title = view.findViewById<TextView>(R.id.onboarding_title)
        val description = view.findViewById<TextView>(R.id.onboarding_description)

        // Set content
        image.setImageResource(R.drawable.ic_secure)
        title.setText(R.string.onboarding_title_secure)
        description.setText(R.string.onboarding_desc_secure)

        // Load animations
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)

        // Apply animations with delays
        image.startAnimation(fadeIn)
        title.apply {
            alpha = 0f
            postDelayed({
                alpha = 1f
                startAnimation(slideUp)
            }, 300)
        }
        description.apply {
            alpha = 0f
            postDelayed({
                alpha = 1f
                startAnimation(slideUp)
            }, 500)

        }
    }
} 