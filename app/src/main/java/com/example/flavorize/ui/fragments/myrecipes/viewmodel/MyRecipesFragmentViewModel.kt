package com.example.flavorize.ui.fragments.myrecipes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.data.Recipe
import kotlinx.coroutines.launch

class MyRecipesFragmentViewModel : ViewModel() {
    private val firestoreRepository = FirestoreRepository()

    private val _myRecipes = MutableLiveData<List<Recipe>>()
    val myRecipes: LiveData<List<Recipe>> get() = _myRecipes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchMyRecipes() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = firestoreRepository.getRecipesByCurrentUser()
            if (result.isSuccess) {
                _myRecipes.value = result.getOrDefault(emptyList())
                _isLoading.value = false
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "An error occurred"
                _isLoading.value = false
            }
        }
    }

    fun refreshMyRecipes() {
        viewModelScope.launch {
            val result = firestoreRepository.getRecipesByCurrentUser()
            if (result.isSuccess) {
                _myRecipes.value = result.getOrDefault(emptyList())
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to refresh recipes"
            }
        }
    }

    fun deleteRecipe(recipeId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = firestoreRepository.deleteRecipe(recipeId)
            if (result.isSuccess) {
                onSuccess()
                refreshMyRecipes() // Refresh data setelah menghapus
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to delete recipe")
            }
        }
    }
}
