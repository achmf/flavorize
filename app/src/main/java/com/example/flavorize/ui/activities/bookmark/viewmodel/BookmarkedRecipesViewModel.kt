package com.example.flavorize.ui.activities.bookmark.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.data.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookmarkedRecipesViewModel : ViewModel() {

    private val firestoreRepository = FirestoreRepository()

    // LiveData to store bookmarked recipes
    private val _bookmarkedRecipes = MutableLiveData<List<Recipe>>()
    val bookmarkedRecipes: LiveData<List<Recipe>> get() = _bookmarkedRecipes

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Fetch bookmarked recipes from the repository
    fun fetchBookmarkedRecipes(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                firestoreRepository.getUserBookmarkedRecipes(userId)
            }
            if (result.isSuccess) {
                _bookmarkedRecipes.value = result.getOrDefault(emptyList())
                _errorMessage.value = null
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
        }
    }

    // Remove a recipe from bookmarks
    fun unbookmarkRecipe(userId: String, recipe: Recipe, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                firestoreRepository.removeBookmark(userId, recipe.id)
            }
            if (result.isSuccess) {
                // Update LiveData by removing the recipe
                _bookmarkedRecipes.value = _bookmarkedRecipes.value?.filter { it.id != recipe.id }
                onSuccess()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }
}
