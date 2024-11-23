package com.example.flavorize.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.flavorize.R
import com.example.flavorize.databinding.ActivityProfileBinding
import com.example.flavorize.ui.auth.LoginActivity
import com.example.flavorize.ui.profile.viewmodel.ProfileActivityViewModel

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up Toolbar
        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true) // Back button
            setHomeAsUpIndicator(R.drawable.ic_back_arrow) // Custom back button icon if needed
            title = "Profile"
        }

        observeViewModel()
        viewModel.loadUserProfile()

        binding.logoutButton.setOnClickListener {
            viewModel.logout()
            navigateToLogin() // Navigate to LoginActivity
        }

        binding.deleteAccountButton.setOnClickListener {
            viewModel.deleteAccount()
        }
    }

    private fun observeViewModel() {
        viewModel.userAvatar.observe(this) { avatarUrl ->
            Glide.with(this)
                .load(avatarUrl)
                .circleCrop()
                .into(binding.userAvatarImageView)
        }

        viewModel.userName.observe(this) { name ->
            binding.userNameTextView.text = name ?: "Unknown User"
        }

        viewModel.userEmail.observe(this) { email ->
            binding.userEmailTextView.text = email ?: "No Email"
        }

        viewModel.isDeleted.observe(this) { isDeleted ->
            if (isDeleted) {
                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            } else {
                Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle back button
                finish()
                true
            }
            R.id.action_help -> {
                // Handle help action
                Toast.makeText(this, "Help clicked", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
