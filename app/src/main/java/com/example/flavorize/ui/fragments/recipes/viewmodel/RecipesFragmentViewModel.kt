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

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // LiveData for bookmark updates
    private val _bookmarkUpdates = MutableLiveData<Set<String>>()
    val bookmarkUpdates: LiveData<Set<String>> get() = _bookmarkUpdates

    // Get paged recipes for the fragment
    fun getPagedRecipes(): Flow<PagingData<Recipe>> {
        return firestoreRepository.getPagedRecipes().cachedIn(viewModelScope)
    }

    // Toggle the bookmark status of a recipe
    fun toggleBookmark(
        recipe: Recipe,
        isBookmarking: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = firestoreRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val result = if (isBookmarking) {
                firestoreRepository.addBookmark(userId, recipe.id) // Add bookmark
            } else {
                firestoreRepository.removeBookmark(userId, recipe.id) // Remove bookmark
            }
            if (result.isSuccess) {
                onSuccess() // Notify success
                recipe.isBookmarked = isBookmarking // Update local state
            } else {
                // Notify error
                _errorMessage.postValue(result.exceptionOrNull()?.message ?: "An error occurred")
                onError(result.exceptionOrNull()?.message ?: "An error occurred")
            }
        }
    }

    // Start listening for bookmark changes in Firestore
    fun startListeningForBookmarkChanges(userId: String) {
        firestoreRepository.listenForBookmarkChanges(userId) { updatedBookmarks ->
            _bookmarkUpdates.postValue(updatedBookmarks) // Update LiveData with new bookmarks
        }
    }

    // Stop listening for bookmark changes in Firestore
    fun stopListeningForBookmarkChanges() {
        firestoreRepository.stopListeningForBookmarkChanges()
    }
}
