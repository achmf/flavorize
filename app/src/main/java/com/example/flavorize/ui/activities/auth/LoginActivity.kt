package com.example.flavorize.ui.activities.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.flavorize.ui.activities.main.MainActivity
import com.example.flavorize.R
import com.example.flavorize.databinding.ActivityLoginBinding
import com.example.flavorize.ui.activities.auth.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val viewModel: LoginViewModel by viewModels()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure Google Sign In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        // Initialize Google Sign-In client
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        viewModel.setGoogleSignInClient(googleSignInClient)

        // Set up login button click listener
        binding.loginButton.setOnClickListener {
            signIn() // Start sign-in process
        }

        // Observe sign-in success
        viewModel.isSignInSuccessful.observe(this) { isSuccessful ->
            if (isSuccessful) {
                // Save user data to Firestore and navigate to MainActivity
                auth.currentUser?.let { user ->
                    viewModel.saveUserToFirestore(user.uid, user.displayName ?: "Unknown User")
                }
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                // Log error messages for debugging
                android.util.Log.e("LoginActivity", it)
            }
        }
    }

    // Launcher for Google Sign-In activity
    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                viewModel.firebaseAuthWithGoogle(account.idToken!!) // Authenticate with Firebase
            } catch (e: ApiException) {
                viewModel.setErrorMessage(e.message) // Handle sign-in failure
            }
        }
    }

    private fun signIn() {
        // Sign out any previous account and launch Google Sign-In
        viewModel.signOutPreviousAccount().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            signInLauncher.launch(signInIntent)
        }
    }
}
