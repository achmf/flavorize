package com.example.flavorize.ui.fragments.recipes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.data.Recipe
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RecipesFragmentViewModel : ViewModel() {
    private val firestoreRepository = FirestoreRepository()

    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> get() = _recipes

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _bookmarkChanges = MutableLiveData<Boolean>()
    val bookmarkChanges: LiveData<Boolean> get() = _bookmarkChanges

    var shouldRefreshOnResume = false // Control refresh on resume

    fun notifyBookmarkChange() {
        shouldRefreshOnResume = true
        _bookmarkChanges.value = true
    }

    fun resetBookmarkChangeFlag() {
        shouldRefreshOnResume = false
    }

    fun getPagedRecipes(): Flow<PagingData<Recipe>> {
        return firestoreRepository.getPagedRecipes().cachedIn(viewModelScope)
    }

    fun toggleBookmark(
        recipe: Recipe,
        isBookmarking: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            val result = if (isBookmarking) {
                firestoreRepository.addBookmark(userId, recipe.id)
            } else {
                firestoreRepository.removeBookmark(userId, recipe.id)
            }
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "An error occurred")
            }
        }
    }
}
