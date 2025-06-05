package com.example.flavorize.ui.fragments.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flavorize.R
import com.example.flavorize.data.api.MealDbRecipe
import com.example.flavorize.data.api.TheMealDbRepository
import kotlinx.coroutines.launch

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

    // LiveData for MealDB recipes
    private val _recipes = MutableLiveData<List<MealDbRecipe>>()
    val recipes: LiveData<List<MealDbRecipe>> get() = _recipes

    // API repository
    private val repository = TheMealDbRepository()

    // Loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Error state
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

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

        // Load recipes when ViewModel is created
        loadRandomRecipes()
    }

    // Update the current image index
    fun updateCurrentImageIndex() {
        val currentIndex = _currentImageIndex.value ?: 0
        val totalImages = _images.value?.size ?: 1
        _currentImageIndex.value = (currentIndex + 1) % totalImages
    }

    // Load random recipes from TheMealDB API
    fun loadRandomRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.getRandomMeals(10)
                if (result.isSuccess) {
                    _recipes.value = result.getOrNull() ?: emptyList()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Unknown error occurred"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Refresh the recipes
    fun refreshRecipes() {
        loadRandomRecipes()
    }
}
