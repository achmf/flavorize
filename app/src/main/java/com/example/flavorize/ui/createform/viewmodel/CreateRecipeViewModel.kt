package com.example.flavorize.ui.createform.viewmodel

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

    private val _addRecipeResult = MutableLiveData<Result<Void?>>()
    val addRecipeResult: LiveData<Result<Void?>> get() = _addRecipeResult
    private var addRecipeJob: Job? = null

    fun addRecipe(recipe: Recipe) {
        addRecipeJob?.cancel()
        addRecipeJob = viewModelScope.launch {
            val result = firestoreRepository.addRecipe(recipe)
            _addRecipeResult.value = result
        }
    }

    override fun onCleared() {
        super.onCleared()
        addRecipeJob?.cancel()
    }
}
