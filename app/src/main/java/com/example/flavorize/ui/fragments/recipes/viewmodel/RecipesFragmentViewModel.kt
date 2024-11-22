package com.example.flavorize.ui.fragments.recipes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.data.Recipe
import kotlinx.coroutines.launch

class RecipesFragmentViewModel : ViewModel() {

    private val firestoreRepository = FirestoreRepository()

    // LiveData untuk daftar resep
    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> get() = _recipes

    // LiveData untuk status loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData untuk pesan error
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Fungsi untuk memuat data resep dari Firestore
    fun fetchRecipes() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = firestoreRepository.getAllRecipes()
            if (result.isSuccess) {
                _recipes.value = result.getOrDefault(emptyList())
                _isLoading.value = false
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "An error occurred"
                _isLoading.value = false
            }
        }
    }
}
