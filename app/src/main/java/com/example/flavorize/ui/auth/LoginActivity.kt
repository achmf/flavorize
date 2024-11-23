package com.example.flavorize.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.flavorize.MainActivity
import com.example.flavorize.R
import com.example.flavorize.databinding.ActivityLoginBinding
import com.example.flavorize.ui.auth.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
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

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile() // Meminta akses ke profil dasar
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        viewModel.setGoogleSignInClient(googleSignInClient)

        binding.loginButton.setOnClickListener {
            signIn()
        }

        viewModel.isSignInSuccessful.observe(this, Observer { isSuccessful ->
            if (isSuccessful) {
                // Save user information to Firestore
                auth.currentUser?.let { user ->
                    viewModel.saveUserToFirestore(user.uid, user.displayName ?: "Unknown User")
                }
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        })

        viewModel.errorMessage.observe(this, Observer { message ->
            message?.let {
                // Show error message to the user
                android.util.Log.e("LoginActivity", it)
            }
        })
    }

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                viewModel.firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                viewModel.setErrorMessage(e.message)
            }
        }
    }

    private fun signIn() {
        viewModel.signOutPreviousAccount().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            signInLauncher.launch(signInIntent)
        }
    }
}