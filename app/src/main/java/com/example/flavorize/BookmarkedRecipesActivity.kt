package com.example.flavorize

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.databinding.ActivityBookmarkedRecipesBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookmarkedRecipesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookmarkedRecipesBinding
    private lateinit var bookmarkedRecipesAdapter: BookmarkedRecipesAdapter
    private val firestoreRepository = FirestoreRepository()
    private val userId: String by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarkedRecipesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        fetchBookmarkedRecipes()
    }

    private fun setupRecyclerView() {
        bookmarkedRecipesAdapter = BookmarkedRecipesAdapter(mutableListOf())
        binding.bookmarkedRecipesRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.bookmarkedRecipesRecyclerView.adapter = bookmarkedRecipesAdapter
    }

    private fun fetchBookmarkedRecipes() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) { firestoreRepository.getUserBookmarkedRecipes(userId) }
            if (result.isSuccess) {
                val recipes = result.getOrDefault(emptyList())
                if (recipes.isEmpty()) {
                    Toast.makeText(
                        this@BookmarkedRecipesActivity,
                        "No bookmarked recipes found.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                bookmarkedRecipesAdapter.updateBookmarkedRecipes(recipes)
            } else {
                Toast.makeText(
                    this@BookmarkedRecipesActivity,
                    "Failed to fetch bookmarked recipes: ${result.exceptionOrNull()?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}
