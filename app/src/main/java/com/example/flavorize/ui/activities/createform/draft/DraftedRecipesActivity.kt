package com.example.flavorize.ui.activities.createform.draft

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flavorize.data.recipedraft.DraftRecipe
import com.example.flavorize.data.recipedraft.DraftRecipeDatabase
import com.example.flavorize.databinding.ActivityDraftedRecipesBinding
import com.example.flavorize.ui.activities.createform.CreateRecipeFormActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class DraftedRecipesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDraftedRecipesBinding
    private val draftDao by lazy { DraftRecipeDatabase.getDatabase(this).draftRecipeDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDraftedRecipesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        loadDrafts() // Fetch data setiap kali aktivitas dilanjutkan
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed() // Handle back button click
        }
    }

    private fun setupUI() {
        binding.draftRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadDrafts() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Ambil userId

        if (userId == null) {
            Toast.makeText(this, "Please login to view drafts", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val drafts = draftDao.getDraftsByUser(userId) // Ambil draft berdasarkan userId
            Log.d("DraftListActivity", "Drafts: $drafts")
            if (drafts.isEmpty()) {
                Toast.makeText(this@DraftedRecipesActivity, "No drafts available", Toast.LENGTH_SHORT).show()
            }
            binding.draftRecyclerView.adapter = DraftedRecipesAdapter(drafts) { draft ->
                showDraftOptionsDialog(draft)
            }
        }
    }


    private fun showDraftOptionsDialog(draft: DraftRecipe) {
        val options = arrayOf("Open Draft", "Delete Draft")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an Option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> navigateToCreateRecipe(draft) // Create Recipe
                1 -> deleteDraft(draft) // Delete Draft
            }
        }
        builder.show()
    }

    private fun navigateToCreateRecipe(draft: DraftRecipe) {
        val intent = Intent(this, CreateRecipeFormActivity::class.java).apply {
            putExtra("draft", draft)
        }
        startActivity(intent)
    }

    private fun deleteDraft(draft: DraftRecipe) {
        lifecycleScope.launch {
            draftDao.deleteDraft(draft)
            Toast.makeText(this@DraftedRecipesActivity, "Draft deleted", Toast.LENGTH_SHORT).show()
            loadDrafts() // Refresh data draft setelah penghapusan
        }
    }
}
