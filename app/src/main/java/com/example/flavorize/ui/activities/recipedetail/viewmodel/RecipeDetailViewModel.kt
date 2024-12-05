package com.example.flavorize.ui.activities.recipedetail.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.data.Recipe
import com.example.flavorize.data.RecipeComment
import kotlinx.coroutines.launch

class RecipeDetailViewModel : ViewModel() {

    private val firestoreRepository = FirestoreRepository()

    // LiveData to hold the recipe details
    private val _recipe = MutableLiveData<Recipe>()
    val recipe: LiveData<Recipe> get() = _recipe

    // LiveData to hold the comments for the recipe
    private val _comments = MutableLiveData<List<RecipeComment>>()
    val comments: LiveData<List<RecipeComment>> get() = _comments

    // LiveData to track loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData to hold error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Set the recipe and fetch comments for it
    fun setRecipe(recipe: Recipe) {
        _recipe.value = recipe
        fetchComments(recipe.id)
    }

    // Fetch comments for a specific recipe ID
    fun fetchComments(recipeId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = firestoreRepository.getCommentsForRecipe(recipeId)
            if (result.isSuccess) {
                _comments.value = result.getOrDefault(emptyList())
                _errorMessage.value = null
            } else {
                _errorMessage.value = "Failed to load comments"
            }
            _isLoading.value = false
        }
    }

    // Add a new comment to the recipe
    fun addComment(comment: RecipeComment) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = firestoreRepository.addCommentToRecipe(comment.recipeId, comment)
            if (result.isSuccess) {
                fetchComments(comment.recipeId)
                _errorMessage.value = null
            } else {
                _errorMessage.value = "Failed to add comment"
            }
            _isLoading.value = false
        }
    }

    // Update an existing comment
    fun updateComment(comment: RecipeComment) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = firestoreRepository.updateComment(comment)
            if (result.isSuccess) {
                fetchComments(comment.recipeId)
                _errorMessage.value = null
            } else {
                _errorMessage.value = "Failed to update comment"
            }
            _isLoading.value = false
        }
    }

    // Delete a comment from the recipe
    fun deleteComment(comment: RecipeComment) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = firestoreRepository.deleteComment(comment)
            if (result.isSuccess) {
                fetchComments(comment.recipeId)
                _errorMessage.value = null
            } else {
                _errorMessage.value = "Failed to delete comment"
            }
            _isLoading.value = false
        }
    }
}
