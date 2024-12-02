package com.example.flavorize.ui.activities.bookmark

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.data.Recipe
import com.example.flavorize.databinding.ActivityBookmarkedRecipesBinding
import com.example.flavorize.ui.fragments.recipes.viewmodel.RecipesFragmentViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookmarkedRecipesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookmarkedRecipesBinding
    private lateinit var bookmarkedRecipesAdapter: BookmarkedRecipesAdapter
    private val recipesViewModel: RecipesFragmentViewModel by viewModels()
    private val firestoreRepository = FirestoreRepository()
    private val userId: String by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarkedRecipesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        fetchBookmarkedRecipes()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        bookmarkedRecipesAdapter = BookmarkedRecipesAdapter(
            mutableListOf(),
            onBookmarkToggle = { recipe -> unbookmarkRecipe(recipe) }
        )
        binding.bookmarkedRecipesRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.bookmarkedRecipesRecyclerView.adapter = bookmarkedRecipesAdapter
    }

    private fun fetchBookmarkedRecipes() {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) { firestoreRepository.getUserBookmarkedRecipes(userId) }
            if (result.isSuccess) {
                val recipes = result.getOrDefault(emptyList())
                bookmarkedRecipesAdapter.updateBookmarkedRecipes(recipes)
                if (recipes.isEmpty()) {
                    showToast("No bookmarked recipes found.")
                }
            } else {
                showToast("Failed to fetch bookmarked recipes: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    private fun unbookmarkRecipe(recipe: Recipe) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) { firestoreRepository.removeBookmark(userId, recipe.id) }
            if (result.isSuccess) {
                bookmarkedRecipesAdapter.removeRecipe(recipe)
                recipesViewModel.notifyBookmarkChange() // Sync changes with ViewModel
                showToast("Bookmark removed!")
            } else {
                showToast("Failed to remove bookmark: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
