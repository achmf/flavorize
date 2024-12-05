package com.example.flavorize.ui.activities.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // LiveData for user profile
    private val _userAvatar = MutableLiveData<String?>()
    val userAvatar: LiveData<String?> get() = _userAvatar

    private val _userName = MutableLiveData<String?>()
    val userName: LiveData<String?> get() = _userName

    private val _userEmail = MutableLiveData<String?>()
    val userEmail: LiveData<String?> get() = _userEmail

    // LiveData for search bar visibility
    private val _isSearchBarVisible = MutableLiveData<Boolean>()
    val isSearchBarVisible: LiveData<Boolean> get() = _isSearchBarVisible

    init {
        loadUserProfile() // Load user profile data
        _isSearchBarVisible.value = false // Search bar is hidden by default
    }

    // Load user profile from Firebase
    private fun loadUserProfile() {
        val user: FirebaseUser? = auth.currentUser
        if (user != null) {
            _userAvatar.value = user.photoUrl?.toString()
            _userName.value = user.displayName
            _userEmail.value = user.email
        }
    }

    // Show the search bar
    fun showSearchBar() {
        _isSearchBarVisible.value = true
    }

    // Hide the search bar
    fun hideSearchBar() {
        _isSearchBarVisible.value = false
    }
}
