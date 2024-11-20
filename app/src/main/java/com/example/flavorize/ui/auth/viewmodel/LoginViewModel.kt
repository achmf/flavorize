package com.example.flavorize.ui.auth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.tasks.Task

class LoginViewModel : ViewModel() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isSignInSuccessful = MutableLiveData<Boolean>()
    val isSignInSuccessful: LiveData<Boolean> get() = _isSignInSuccessful

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun setGoogleSignInClient(client: GoogleSignInClient) {
        googleSignInClient = client
    }

    fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _isSignInSuccessful.value = true
                } else {
                    _errorMessage.value = task.exception?.message
                }
            }
    }

    fun signOutPreviousAccount(): Task<Void> {
        return googleSignInClient.signOut()
    }

    fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }
}