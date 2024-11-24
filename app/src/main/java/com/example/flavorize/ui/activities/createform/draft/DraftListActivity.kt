package com.example.flavorize.ui.activities.createform.draft

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flavorize.data.recipedraft.DraftRecipeDatabase
import com.example.flavorize.databinding.ActivityDraftListBinding
import kotlinx.coroutines.launch

class DraftListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDraftListBinding
    private val draftDao by lazy { DraftRecipeDatabase.getDatabase(this).draftRecipeDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDraftListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadDrafts()
    }

    private fun setupUI() {
        binding.draftRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadDrafts() {
        lifecycleScope.launch {
            val drafts = draftDao.getAllDrafts()
            Log.d("DraftListActivity", "Drafts: $drafts")
            // Tampilkan di RecyclerView
            binding.draftRecyclerView.adapter = DraftListAdapter(drafts)
        }
    }
}
