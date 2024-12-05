package com.example.flavorize.ui.activities.profile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ProfileActivityViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // LiveData for user avatar URL
    private val _userAvatar = MutableLiveData<String?>()
    val userAvatar: LiveData<String?> get() = _userAvatar

    // LiveData for user name
    private val _userName = MutableLiveData<String?>()
    val userName: LiveData<String?> get() = _userName

    // LiveData for user email
    private val _userEmail = MutableLiveData<String?>()
    val userEmail: LiveData<String?> get() = _userEmail

    // LiveData to track account deletion status
    private val _isDeleted = MutableLiveData<Boolean>()
    val isDeleted: LiveData<Boolean> get() = _isDeleted

    // Load user profile details
    fun loadUserProfile() {
        val user: FirebaseUser? = auth.currentUser
        if (user != null) {
            _userAvatar.value = user.photoUrl?.toString()
            _userName.value = user.displayName
            _userEmail.value = user.email
        }
    }

    // Sign out the user
    fun logout() {
        auth.signOut()
    }

    // Delete the current user account
    fun deleteAccount() {
        auth.currentUser?.let { user ->
            user.delete().addOnCompleteListener { task ->
                _isDeleted.value = task.isSuccessful
            }
        }
    }
}
