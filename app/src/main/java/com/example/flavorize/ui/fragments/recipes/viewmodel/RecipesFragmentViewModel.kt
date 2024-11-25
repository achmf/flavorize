package com.example.flavorize.ui.fragments.recipes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.data.Recipe
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RecipesFragmentViewModel : ViewModel() {
    private val firestoreRepository = FirestoreRepository()

    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> get() = _recipes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchRecipesWithBookmarks() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        _isLoading.value = true
        viewModelScope.launch {
            val result = firestoreRepository.fetchRecipesWithBookmarkStatus(userId)
            if (result.isSuccess) {
                _recipes.value = result.getOrDefault(emptyList())
                _isLoading.value = false
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
                _isLoading.value = false
            }
        }
    }

    fun toggleBookmark(
        recipe: Recipe,
        isBookmarking: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            val result = if (isBookmarking) {
                firestoreRepository.addBookmark(userId, recipe.id)
            } else {
                firestoreRepository.removeBookmark(userId, recipe.id)
            }
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "An error occurred")
            }
        }
    }
}
