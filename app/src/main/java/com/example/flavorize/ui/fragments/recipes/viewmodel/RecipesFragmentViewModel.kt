package com.example.flavorize.ui.fragments.recipes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.data.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RecipesFragmentViewModel : ViewModel() {
    private val firestoreRepository = FirestoreRepository()

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _bookmarkUpdates = MutableLiveData<Set<String>>()
    val bookmarkUpdates: LiveData<Set<String>> get() = _bookmarkUpdates


    fun notifyBookmarkChange() {
        val userId = firestoreRepository.getCurrentUserId() ?: return
        startListeningForBookmarkChanges(userId)
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
        val userId = firestoreRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val result = if (isBookmarking) {
                firestoreRepository.addBookmark(userId, recipe.id)
            } else {
                firestoreRepository.removeBookmark(userId, recipe.id)
            }
            if (result.isSuccess) {
                onSuccess()
                // Update the recipe's local state
                recipe.isBookmarked = isBookmarking
            } else {
                _errorMessage.postValue(result.exceptionOrNull()?.message ?: "An error occurred")
                onError(result.exceptionOrNull()?.message ?: "An error occurred")
            }
        }
    }

    fun startListeningForBookmarkChanges(userId: String) {
        firestoreRepository.listenForBookmarkChanges(userId) { updatedBookmarks ->
            _bookmarkUpdates.postValue(updatedBookmarks)
        }
    }

    fun stopListeningForBookmarkChanges() {
        firestoreRepository.stopListeningForBookmarkChanges()
    }
}
