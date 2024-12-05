package com.example.flavorize.ui.activities.editrecipe.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.data.Recipe
import kotlinx.coroutines.launch

class EditRecipeViewModel : ViewModel() {
    private val firestoreRepository = FirestoreRepository()

    // LiveData for the recipe being edited
    private val _recipe = MutableLiveData<Recipe>()
    val recipe: LiveData<Recipe> get() = _recipe

    // LiveData for update result
    private val _updateResult = MutableLiveData<Result<Void?>>()
    val updateResult: LiveData<Result<Void?>> get() = _updateResult

    // Set the current recipe to be edited
    fun setRecipe(recipe: Recipe) {
        _recipe.value = recipe
    }

    // Update the recipe in Firestore
    fun updateRecipe(updatedRecipe: Recipe) {
        viewModelScope.launch {
            val result = firestoreRepository.updateRecipe(updatedRecipe.id, updatedRecipe)
            _updateResult.value = result
        }
    }
}
