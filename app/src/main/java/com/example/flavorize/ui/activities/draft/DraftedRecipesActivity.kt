package com.example.flavorize.ui.activities.draft

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flavorize.data.recipedraft.DraftRecipe
import com.example.flavorize.data.recipedraft.DraftRecipeDatabase
import com.example.flavorize.databinding.ActivityDraftedRecipesBinding
import com.example.flavorize.databinding.DialogConfirmDraftDeleteBinding
import com.example.flavorize.databinding.DialogDraftOptionsBinding
import com.example.flavorize.ui.activities.createform.CreateRecipeFormActivity
import com.example.flavorize.ui.activities.draft.viewmodel.DraftedRecipesViewModel
import com.example.flavorize.ui.activities.draft.viewmodel.DraftedRecipesViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class DraftedRecipesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDraftedRecipesBinding
    private lateinit var viewModel: DraftedRecipesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDraftedRecipesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val draftDao = DraftRecipeDatabase.getDatabase(this).draftRecipeDao()
        viewModel = ViewModelProvider(this,
            DraftedRecipesViewModelFactory(draftDao))[DraftedRecipesViewModel::class.java]

        setupToolbar() // Setup toolbar
        setupUI() // Setup RecyclerView
        setupObservers() // Observe LiveData
    }

    override fun onResume() {
        super.onResume()
        loadDrafts() // Reload drafts when activity resumes
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed() // Handle back button
        }
    }

    private fun setupUI() {
        // Set up RecyclerView
        binding.draftRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupObservers() {
        // Observe drafts LiveData
        viewModel.drafts.observe(this) { drafts ->
            if (drafts.isEmpty()) {
                Toast.makeText(this, "No drafts available", Toast.LENGTH_SHORT).show()
            }
            binding.draftRecyclerView.adapter = DraftedRecipesAdapter(drafts) { draft ->
                showDraftOptionsDialog(draft) // Show draft options
            }
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadDrafts() {
        // Get the user ID and fetch drafts
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Please login to view drafts", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.fetchDrafts(userId)
        }
    }

    private fun showDraftOptionsDialog(draft: DraftRecipe) {
        // Inflate custom view with ViewBinding
        val dialogBinding = DialogDraftOptionsBinding.inflate(layoutInflater)

        // Create the AlertDialog instance
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        // Set click listeners for buttons
        dialogBinding.openDraftButton.setOnClickListener {
            navigateToCreateRecipe(draft) // Open draft
            dialog.dismiss() // Dismiss dialog after action
        }

        dialogBinding.deleteDraftButton.setOnClickListener {
            deleteDraft(draft) // Delete draft
            dialog.dismiss() // Dismiss dialog after action
        }

        // Apply rounded corners to the dialog
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun navigateToCreateRecipe(draft: DraftRecipe) {
        // Navigate to CreateRecipeFormActivity with the draft
        val intent = Intent(this, CreateRecipeFormActivity::class.java).apply {
            putExtra("draft", draft)
        }
        startActivity(intent)
    }

    private fun deleteDraft(draft: DraftRecipe) {
        // Inflate custom view with ViewBinding
        val dialogBinding = DialogConfirmDraftDeleteBinding.inflate(layoutInflater)

        // Create the AlertDialog instance
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        // Set dialog texts dynamically (if needed)
        dialogBinding.confirmDialogTitle.text
        dialogBinding.confirmDialogMessage.text

        // Set click listeners for buttons
        dialogBinding.cancelButton.setOnClickListener {
            dialog.dismiss() // Dismiss dialog if user cancels
        }

        dialogBinding.confirmButton.setOnClickListener {
            // Proceed with deletion
            viewModel.deleteDraft(draft) {
                Toast.makeText(this, "Draft deleted", Toast.LENGTH_SHORT).show()
                loadDrafts() // Refresh the drafts list
            }
            dialog.dismiss() // Dismiss dialog after confirmation
        }

        // Apply rounded corners to the dialog
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}
