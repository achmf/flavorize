package com.example.flavorize.ui.activities.draft.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flavorize.data.recipedraft.DraftRecipeDao

class DraftedRecipesViewModelFactory(
    private val draftRecipeDao: DraftRecipeDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DraftedRecipesViewModel::class.java)) {
            return DraftedRecipesViewModel(draftRecipeDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
