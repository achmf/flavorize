package com.example.flavorize.ui.fragments.myrecipe.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.data.Recipe
import kotlinx.coroutines.launch

class MyRecipesFragmentViewModel : ViewModel() {
    private val firestoreRepository = FirestoreRepository()

    // LiveData untuk daftar resep pengguna
    private val _myRecipes = MutableLiveData<List<Recipe>>()
    val myRecipes: LiveData<List<Recipe>> get() = _myRecipes

    // LiveData untuk status loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData untuk pesan error
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Fungsi untuk memuat data resep pengguna dari Firestore
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
}
