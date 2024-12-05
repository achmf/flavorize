package com.example.flavorize.ui.activities.createform.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.data.Recipe
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CreateRecipeViewModel : ViewModel() {
    private val firestoreRepository = FirestoreRepository()

    // LiveData to observe the result of adding a recipe
    private val _addRecipeResult = MutableLiveData<Result<Void?>>()
    val addRecipeResult: LiveData<Result<Void?>> get() = _addRecipeResult

    private var addRecipeJob: Job? = null // Job to manage the add recipe coroutine

    // Add a new recipe to Firestore
    fun addRecipe(recipe: Recipe) {
        addRecipeJob?.cancel() // Cancel any ongoing job
        addRecipeJob = viewModelScope.launch {
            val result = firestoreRepository.addRecipe(recipe)
            _addRecipeResult.value = result // Update the LiveData with the result
        }
    }

    // Clean up when the ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        addRecipeJob?.cancel() // Cancel any remaining job
    }
}
