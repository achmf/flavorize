package com.example.flavorize.ui.fragments.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.flavorize.databinding.FragmentHomeBinding
import com.example.flavorize.ui.fragments.home.viewmodel.HomeFragmentViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // ViewModel instance
    private val viewModel: HomeFragmentViewModel by viewModels()

    private val handler = Handler(Looper.getMainLooper())
    private val imageSwitcherRunnable = object : Runnable {
        override fun run() {
            viewModel.updateCurrentImageIndex() // Update image index using ViewModel
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
        observeViewModel()
    }

    private fun setupImageSwitcher() {
        binding.imageCarousel.setFactory {
            val imageView = ImageView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            imageView
        }

        // Start the image switching animation
        handler.post(imageSwitcherRunnable)
    }

    private fun observeViewModel() {
        // Observe images LiveData and set the first image
        viewModel.images.observe(viewLifecycleOwner) { images ->
            if (images.isNotEmpty()) {
                binding.imageCarousel.setImageResource(
                    images[viewModel.currentImageIndex.value ?: 0]
                )
            }
        }

        // Observe the current image index and update the ImageSwitcher
        viewModel.currentImageIndex.observe(viewLifecycleOwner) { index ->
            val images = viewModel.images.value ?: emptyList()
            if (images.isNotEmpty()) {
                binding.imageCarousel.setImageResource(images[index])
            }
        }

        // Observe dynamic info content and update the TextView
        viewModel.dynamicInfoContent.observe(viewLifecycleOwner) { content ->
            binding.infoContent.text = content
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacks(imageSwitcherRunnable) // Stop the handler when the fragment is destroyed
    }
}
