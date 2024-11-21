package com.example.flavorize

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _navigateToCreateRecipe = MutableLiveData<Boolean>()
    val navigateToCreateRecipe: LiveData<Boolean>
        get() = _navigateToCreateRecipe

    fun onCreateRecipeClicked() {
        viewModelScope.launch {
            _navigateToCreateRecipe.value = true
        }
    }

    fun onNavigatedToCreateRecipe() {
        viewModelScope.launch {
            _navigateToCreateRecipe.value = false
        }
    }
}
