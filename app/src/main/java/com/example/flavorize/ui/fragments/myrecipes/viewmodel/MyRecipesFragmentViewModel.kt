package com.example.flavorize.ui.fragments.myrecipes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.data.Recipe
import kotlinx.coroutines.flow.Flow

class MyRecipesFragmentViewModel : ViewModel() {
    val firestoreRepository = FirestoreRepository()

    fun getPagedMyRecipes(userId: String): Flow<PagingData<Recipe>> {
        return firestoreRepository.getPagedMyRecipes(userId).cachedIn(viewModelScope)
    }
}
