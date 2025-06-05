package com.example.flavorize.ui.fragments.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flavorize.R
import com.example.flavorize.data.api.MealDbRecipe
import com.example.flavorize.data.api.TheMealDbRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    // Search query
    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> get() = _searchQuery

    // Search mode active
    private val _isSearchActive = MutableLiveData<Boolean>(false)
    val isSearchActive: LiveData<Boolean> get() = _isSearchActive

    // Variable to hold search job for debouncing
    private var searchJob: Job? = null

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
        if (_isSearchActive.value == true) {
            // If search is active, refresh the search results
            searchMeals(_searchQuery.value ?: "")
        } else {
            // Otherwise, load random recipes
            loadRandomRecipes()
        }
    }

    // Set search query and trigger search
    fun setSearchQuery(query: String) {
        _searchQuery.value = query

        // If query is empty and search was active, load random recipes
        if (query.isEmpty() && _isSearchActive.value == true) {
            _isSearchActive.value = false
            loadRandomRecipes()
            return
        }

        // Debounce search requests to avoid making too many API calls
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Wait for 500ms before searching
            searchMeals(query)
        }
    }

    // Search meals by name
    private fun searchMeals(query: String) {
        if (query.isBlank()) return

        _isSearchActive.value = true
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val result = repository.searchMeals(query)
                if (result.isSuccess) {
                    _recipes.value = result.getOrNull() ?: emptyList()
                    if ((result.getOrNull() ?: emptyList()).isEmpty()) {
                        _error.value = "No recipes found for '$query'"
                    }
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

    // Clear search and return to random recipes
    fun clearSearch() {
        _searchQuery.value = ""
        _isSearchActive.value = false
        loadRandomRecipes()
    }
}
