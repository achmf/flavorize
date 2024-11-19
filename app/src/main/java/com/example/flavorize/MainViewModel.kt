package com.example.flavorize

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _navigateToCreateRecipe = MutableLiveData<Boolean>()
    val navigateToCreateRecipe: LiveData<Boolean>
        get() = _navigateToCreateRecipe

    fun onCreateRecipeClicked() {
        _navigateToCreateRecipe.value = true
    }

    fun onNavigatedToCreateRecipe() {
        _navigateToCreateRecipe.value = false
    }
}
