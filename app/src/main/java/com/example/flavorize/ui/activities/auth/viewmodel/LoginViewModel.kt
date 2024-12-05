package com.example.flavorize.ui.activities.auth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // LiveData to track sign-in success
    private val _isSignInSuccessful = MutableLiveData<Boolean>()
    val isSignInSuccessful: LiveData<Boolean> get() = _isSignInSuccessful

    // LiveData to hold error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Set the Google Sign-In client
    fun setGoogleSignInClient(client: GoogleSignInClient) {
        googleSignInClient = client
    }

    // Authenticate with Firebase using Google ID token
    fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _isSignInSuccessful.value = true // Notify success
                } else {
                    _errorMessage.value = task.exception?.message // Notify error
                }
            }
    }

    // Save user data to Firestore
    fun saveUserToFirestore(userId: String, userName: String) {
        val userMap = hashMapOf(
            "name" to userName
        )
        // Save user information in Firestore
        viewModelScope.launch {
            firestore.collection("users").document(userId).set(userMap)
        }
    }

    // Sign out the previous Google account
    fun signOutPreviousAccount() = googleSignInClient.signOut()

    // Set an error message
    fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }
}
