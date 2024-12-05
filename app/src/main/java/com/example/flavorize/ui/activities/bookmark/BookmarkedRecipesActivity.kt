package com.example.flavorize.ui.activities.bookmark

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.flavorize.data.Recipe
import com.example.flavorize.databinding.ActivityBookmarkedRecipesBinding
import com.example.flavorize.ui.activities.bookmark.viewmodel.BookmarkedRecipesViewModel
import com.google.firebase.auth.FirebaseAuth

class BookmarkedRecipesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookmarkedRecipesBinding
    private lateinit var bookmarkedRecipesAdapter: BookmarkedRecipesAdapter
    private val viewModel: BookmarkedRecipesViewModel by viewModels()
    private val userId: String by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarkedRecipesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar() // Setup toolbar
        setupRecyclerView() // Setup recycler view
        setupObservers() // Observe ViewModel LiveData

        // Fetch bookmarked recipes
        viewModel.fetchBookmarkedRecipes(userId)
    }

    private fun setupToolbar() {
        // Configure toolbar with back navigation
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        // Initialize the adapter and set up recycler view
        bookmarkedRecipesAdapter = BookmarkedRecipesAdapter(
            mutableListOf(),
            onBookmarkToggle = { recipe -> unbookmarkRecipe(recipe) }
        )
        binding.bookmarkedRecipesRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.bookmarkedRecipesRecyclerView.adapter = bookmarkedRecipesAdapter
    }

    private fun setupObservers() {
        // Observe bookmarked recipes and update UI
        viewModel.bookmarkedRecipes.observe(this) { recipes ->
            bookmarkedRecipesAdapter.updateBookmarkedRecipes(recipes)
            if (recipes.isEmpty()) {
                showToast("No bookmarked recipes found.")
            }
        }

        // Observe error messages and show them as toast
        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                showToast("Error: $it")
            }
        }
    }

    private fun unbookmarkRecipe(recipe: Recipe) {
        // Handle unbookmark action
        viewModel.unbookmarkRecipe(userId, recipe) {
            showToast("Bookmark removed!")
        }
    }

    private fun showToast(message: String) {
        // Show a toast message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
