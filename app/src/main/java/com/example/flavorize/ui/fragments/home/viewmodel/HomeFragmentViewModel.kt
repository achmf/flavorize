package com.example.flavorize.ui.fragments.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flavorize.R

class HomeFragmentViewModel : ViewModel() {

    // LiveData for the list of images
    private val _images = MutableLiveData<List<Int>>()
    val images: LiveData<List<Int>> get() = _images

    // LiveData for the current image index
    private val _currentImageIndex = MutableLiveData<Int>()
    val currentImageIndex: LiveData<Int> get() = _currentImageIndex

    // LiveData for dynamic info content
    private val _dynamicInfoContent = MutableLiveData<String>()
    val dynamicInfoContent: LiveData<String> get() = _dynamicInfoContent

    init {
        // Initialize the image list and index
        _images.value = listOf(
            R.drawable.image1,
            R.drawable.image2,
            R.drawable.image3,
            R.drawable.image4,
            R.drawable.image5
        )
        _currentImageIndex.value = 0

        // Initialize dynamic content
        _dynamicInfoContent.value = """
            • Discover thousands of curated recipes.
            • Share your favorite recipes with the community.
            • Save your favorite recipes in bookmarks.
            • Get inspired with daily cooking ideas.
            • Easy-to-use app for all food enthusiasts.
        """.trimIndent()
    }

    // Update the current image index
    fun updateCurrentImageIndex() {
        val currentIndex = _currentImageIndex.value ?: 0
        val totalImages = _images.value?.size ?: 1
        _currentImageIndex.value = (currentIndex + 1) % totalImages
    }
}
