package com.example.flavorize.ui.activities.recipedetail

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.flavorize.R
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.data.Recipe
import com.example.flavorize.data.RecipeComment
import com.example.flavorize.databinding.ActivityRecipeDetailBinding
import com.example.flavorize.ui.activities.recipedetail.viewmodel.RecipeDetailViewModel
import kotlinx.coroutines.launch

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailBinding
    private lateinit var viewModel: RecipeDetailViewModel
    private val commentsAdapter = RecipeCommentsAdapter(
        currentUserId = FirestoreRepository().getCurrentUserId() ?: "", // Current user ID
        onCommentLongClick = { comment, view ->
            showPopupMenu(comment, view) // Show popup menu on long click
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[RecipeDetailViewModel::class.java]

        setupToolbar() // Setup the toolbar
        setupSwipeToRefresh() // Setup swipe-to-refresh functionality
        setupObservers() // Observe LiveData from ViewModel
        setupCommentsSection() // Setup comments section

        // Get recipe data from intent
        val recipe = intent.getParcelableExtra<Recipe>("recipe")
        if (recipe != null) {
            viewModel.setRecipe(recipe)
        } else {
            Toast.makeText(this, "Recipe data not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        // Setup the action bar with a back button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupSwipeToRefresh() {
        // Refresh comments when swipe-to-refresh is triggered
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.recipe.value?.let { recipe ->
                viewModel.fetchComments(recipe.id)
            }
        }
    }

    private fun setupObservers() {
        // Observe recipe LiveData and bind its details
        viewModel.recipe.observe(this) { recipe ->
            bindRecipeDetails(recipe)
        }

        // Observe comments LiveData and update the comments adapter
        viewModel.comments.observe(this) { comments ->
            commentsAdapter.submitList(comments)
            binding.noCommentsTextView.visibility = if (comments.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe loading state and show/hide the loading indicator
        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        // Observe error messages and display them as Toast
        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindRecipeDetails(recipe: Recipe) {
        // Bind recipe details to the UI
        lifecycleScope.launch {
            val userName = FirestoreRepository().getUserNameById(recipe.userId)
            binding.recipeUserNameTextView.text = getString(R.string.recipe_created_by, userName)
        }

        binding.recipeNameTextView.text = recipe.name
        binding.recipeDescriptionTextView.text = recipe.description
        binding.recipeServingsTextView.text = getString(R.string.recipe_servings, recipe.servings)
        binding.recipeCookingTimeTextView.text = getString(R.string.recipe_cooking_time, recipe.cookingTime)

        val formattedIngredients = recipe.ingredients.mapIndexed { index, ingredient ->
            "${index + 1}. $ingredient"
        }.joinToString("\n\n")
        binding.ingredientsTextView.text = formattedIngredients

        val formattedSteps = recipe.instructions.mapIndexed { index, step ->
            "${index + 1}. $step"
        }.joinToString("\n\n")
        binding.instructionsTextView.text = formattedSteps

        // Load recipe image using Glide
        Glide.with(this)
            .load(recipe.imageUrl)
            .into(binding.recipeImageView)
    }

    private fun setupCommentsSection() {
        // Setup RecyclerView for comments
        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.commentsRecyclerView.adapter = commentsAdapter

        // Add a new comment when the button is clicked
        binding.addCommentButton.setOnClickListener {
            val commentText = binding.commentEditText.text.toString().trim()
            if (commentText.isNotEmpty()) {
                val newComment = RecipeComment(
                    recipeId = viewModel.recipe.value!!.id,
                    userId = FirestoreRepository().getCurrentUserId() ?: return@setOnClickListener,
                    userName = FirestoreRepository().getCurrentUserName(),
                    text = commentText,
                    timestamp = System.currentTimeMillis()
                )
                viewModel.addComment(newComment)
                binding.commentEditText.text?.clear()
                hideKeyboard()
            } else {
                Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hideKeyboard() {
        // Hide the keyboard from the screen
        currentFocus?.let { view ->
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun showPopupMenu(comment: RecipeComment, anchorView: View) {
        // Show a popup menu for editing or deleting a comment
        val popupMenu = PopupMenu(this, anchorView)
        popupMenu.inflate(R.menu.comment_popup_menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    showEditCommentDialog(comment)
                    true
                }
                R.id.action_delete -> {
                    viewModel.deleteComment(comment)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showEditCommentDialog(comment: RecipeComment) {
        // Show a dialog to edit the comment
        val editText = androidx.appcompat.widget.AppCompatEditText(this)
        editText.setText(comment.text)
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Edit Comment")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val updatedText = editText.text.toString()
                if (updatedText.isNotEmpty()) {
                    viewModel.updateComment(comment.copy(text = updatedText))
                } else {
                    Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
