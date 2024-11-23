package com.example.flavorize.ui.fragments.profile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragmentViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _userAvatar = MutableLiveData<String?>()
    val userAvatar: LiveData<String?> get() = _userAvatar

    private val _userName = MutableLiveData<String?>()
    val userName: LiveData<String?> get() = _userName

    private val _userEmail = MutableLiveData<String?>()
    val userEmail: LiveData<String?> get() = _userEmail

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun loadUserProfile() {
        val user: FirebaseUser? = auth.currentUser
        if (user != null) {
            _userAvatar.value = user.photoUrl?.toString()
            _userName.value = user.displayName
            _userEmail.value = user.email
        } else {
            _errorMessage.value = "User not logged in"
        }
    }

    fun deleteAccount(callback: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            // Delete all user recipes
            firestore.collection("recipes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val batch = firestore.batch()
                    for (document in querySnapshot.documents) {
                        batch.delete(document.reference)
                    }
                    batch.commit().addOnCompleteListener { batchTask ->
                        if (batchTask.isSuccessful) {
                            // Delete user document
                            firestore.collection("users").document(userId).delete()
                                .addOnCompleteListener { userDocTask ->
                                    if (userDocTask.isSuccessful) {
                                        // Delete user account
                                        user.delete().addOnCompleteListener { deleteTask ->
                                            if (deleteTask.isSuccessful) {
                                                callback(true, null)
                                            } else {
                                                callback(false, deleteTask.exception?.message)
                                            }
                                        }
                                    } else {
                                        callback(false, userDocTask.exception?.message)
                                    }
                                }
                        } else {
                            callback(false, batchTask.exception?.message)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    callback(false, exception.message)
                }
        } else {
            callback(false, "User not logged in")
        }
    }

    fun logout() {
        auth.signOut()
    }
}
