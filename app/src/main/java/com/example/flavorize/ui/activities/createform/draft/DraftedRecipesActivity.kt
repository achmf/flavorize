package com.example.flavorize.ui.activities.createform.draft

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flavorize.data.recipedraft.DraftRecipeDatabase
import com.example.flavorize.databinding.ActivityDraftedRecipesBinding
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
        loadDrafts()
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
        lifecycleScope.launch {
            val drafts = draftDao.getAllDrafts()
            Log.d("DraftListActivity", "Drafts: $drafts")
            // Tampilkan di RecyclerView
            binding.draftRecyclerView.adapter = DraftedRecipesAdapter(drafts)
        }
    }
}
