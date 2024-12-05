package com.example.flavorize.ui.activities.draft.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flavorize.data.recipedraft.DraftRecipe
import com.example.flavorize.data.recipedraft.DraftRecipeDao
import kotlinx.coroutines.launch

class DraftedRecipesViewModel(private val draftRecipeDao: DraftRecipeDao) : ViewModel() {

    // LiveData for draft list
    private val _drafts = MutableLiveData<List<DraftRecipe>>()
    val drafts: LiveData<List<DraftRecipe>> get() = _drafts

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Fetch drafts for the given user ID
    fun fetchDrafts(userId: String) {
        viewModelScope.launch {
            try {
                val draftList = draftRecipeDao.getDraftsByUser(userId)
                _drafts.value = draftList
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // Delete a specific draft
    fun deleteDraft(draft: DraftRecipe, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                draftRecipeDao.deleteDraft(draft)
                onSuccess() // Callback after successful deletion
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
}
