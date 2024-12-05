package com.example.flavorize.ui.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.flavorize.R
import com.example.flavorize.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val images = listOf(
        R.drawable.image1,
        R.drawable.image2,
        R.drawable.image3,
        R.drawable.image4,
        R.drawable.image5
    )
    private var currentImageIndex = 0

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val imageSwitcherRunnable = object : Runnable {
        override fun run() {
            updateImage()
            handler.postDelayed(this, 5000) // Switch image every 5 seconds
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupImageSwitcher()
        setupDynamicInfoContent()
    }

    private fun setupImageSwitcher() {
        handler.post(imageSwitcherRunnable) // Start image switching
    }

    private fun updateImage() {
        currentImageIndex = (currentImageIndex + 1) % images.size
        binding.imageCarousel.setImageResource(images[currentImageIndex])
    }

    private fun setupDynamicInfoContent() {
        // Example dynamic content
        val dynamicFeatures = listOf(
            "• Discover thousands of curated recipes.",
            "• Share your favorite recipes with the community.",
            "• Save your favorite recipes in bookmarks.",
            "• Get inspired with daily cooking ideas.",
            "• Easy-to-use app for all food enthusiasts."
        )

        // Combine the list into a single string with newlines
        val dynamicContent = dynamicFeatures.joinToString("\n")

        // Set the dynamic content to the TextView
        binding.infoContent.text = dynamicContent
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacks(imageSwitcherRunnable) // Stop handler when the fragment is destroyed
    }
}
