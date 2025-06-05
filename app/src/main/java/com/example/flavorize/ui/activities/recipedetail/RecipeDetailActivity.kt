package com.example.flavorize.ui.activities.recipedetail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
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

    private var isApiRecipe = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[RecipeDetailViewModel::class.java]

        isApiRecipe = intent.getBooleanExtra("is_api_recipe", false)

        setupToolbar() // Setup the toolbar
        setupSwipeToRefresh() // Setup swipe-to-refresh functionality
        setupObservers() // Observe LiveData from ViewModel

        if (isApiRecipe) {
            // Handle recipe from TheMealDB API
            handleApiRecipe()
        } else {
            // Setup comments section for user recipes
            setupCommentsSection()

            // Handle recipe from Firestore - using backward-compatible approach instead of API 33-only method
            val recipe = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("recipe", Recipe::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra("recipe") as Recipe?
            }

            if (recipe != null) {
                viewModel.setRecipe(recipe)
            } else {
                Toast.makeText(this, "Recipe data not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun handleApiRecipe() {
        // Extract data from intent extras
        val mealName = intent.getStringExtra("meal_name") ?: "Unknown Recipe"
        val mealCategory = intent.getStringExtra("meal_category") ?: ""
        val mealArea = intent.getStringExtra("meal_area") ?: ""
        val mealInstructions = intent.getStringExtra("meal_instructions") ?: ""
        val mealImageUrl = intent.getStringExtra("meal_image_url") ?: ""
        val mealYoutubeUrl = intent.getStringExtra("meal_youtube") ?: ""
        val mealIngredients = intent.getStringArrayListExtra("meal_ingredients") ?: ArrayList()

        // Set up collapsing toolbar with recipe name
        binding.collapsingToolbar.title = mealName

        binding.apply {
            // Load recipe image
            Glide.with(this@RecipeDetailActivity)
                .load(mealImageUrl)
                .placeholder(R.drawable.image1)
                .error(R.drawable.image1)
                .into(recipeImageView)

            // Set category and cuisine area as chips
            recipeCategoryChip.text = mealCategory
            recipeCategoryChip.visibility = if (mealCategory.isNotEmpty()) View.VISIBLE else View.GONE

            recipeAreaChip.text = mealArea
            recipeAreaChip.visibility = if (mealArea.isNotEmpty()) View.VISIBLE else View.GONE

            // For API recipes, hide user information section
            recipeUserSection.visibility = View.GONE
            recipeDescriptionTextView.visibility = View.GONE

            // Format and set ingredients
            val ingredientsText = mealIngredients.joinToString("\n• ", "• ")
            recipeIngredientsTextView.text = ingredientsText

            // Format and set instructions with proper paragraphing
            val formattedInstructions = mealInstructions
                .split("\\r\\n|\\n|\\r".toRegex())
                .filter { it.trim().isNotEmpty() }
                .joinToString("\n\n")

            recipeInstructionsTextView.text = formattedInstructions

            // Set up YouTube link if available - using KTX extension
            if (mealYoutubeUrl.isNotEmpty()) {
                youtubeButton.visibility = View.VISIBLE
                youtubeButton.setOnClickListener {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, mealYoutubeUrl.toUri())
                        startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(this@RecipeDetailActivity, "Cannot open YouTube link", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                youtubeButton.visibility = View.GONE
            }

            // Hide comments section for API recipes
            commentsSection.visibility = View.GONE
            submitCommentButton.visibility = View.GONE
            commentEditText.visibility = View.GONE
        }

        // Hide loading indicator
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun setupToolbar() {
        // Setup the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set initial navigation icon color to white (for expanded state)
        binding.toolbar.navigationIcon?.setTint(resources.getColor(android.R.color.white, theme))

        // Add offset change listener to AppBarLayout to detect collapse/expand
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val isCollapsed = Math.abs(verticalOffset) >= appBarLayout.totalScrollRange * 0.8

            // Change navigation icon color based on collapsed state
            val navIconColor = if (isCollapsed) {
                resources.getColor(android.R.color.black, theme) // Black when collapsed
            } else {
                resources.getColor(android.R.color.white, theme) // White when expanded
            }
            binding.toolbar.navigationIcon?.setTint(navIconColor)
        }

        binding.toolbar.setNavigationOnClickListener {
            // Use onBackPressedDispatcher instead of deprecated onBackPressed()
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (isApiRecipe) {
                // For API recipes, just stop refreshing animation
                binding.swipeRefreshLayout.isRefreshing = false
            } else {
                viewModel.recipe.value?.let { recipe ->
                    viewModel.fetchComments(recipe.id)
                }
            }
        }
    }

    private fun setupObservers() {
        // Observe recipe LiveData and bind its details
        viewModel.recipe.observe(this) { recipe ->
            if (!isApiRecipe) {
                bindRecipeDetails(recipe)
            }
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
        // Set up collapsing toolbar with recipe name for Firestore recipes
        binding.collapsingToolbar.title = recipe.name

        // Bind user recipe details to the UI
        lifecycleScope.launch {
            val userName = FirestoreRepository().getUserNameById(recipe.userId)
            binding.recipeUserNameTextView.text = getString(R.string.recipe_created_by, userName)
        }

        binding.apply {
            // Basic recipe information - removed reference to recipeNameTextView
            recipeDescriptionTextView.text = recipe.description
            recipeDescriptionTextView.visibility = View.VISIBLE
            recipeServingsTextView.text = getString(R.string.recipe_servings, recipe.servings)
            recipeCookingTimeTextView.text = getString(R.string.recipe_cooking_time, recipe.cookingTime)

            // User section is visible for Firestore recipes
            recipeUserSection.visibility = View.VISIBLE

            // Format and set ingredients using item numbers for Firestore recipes
            val formattedIngredients = recipe.ingredients.mapIndexed { index, ingredient ->
                "${index + 1}) $ingredient"
            }.joinToString("\n\n")
            recipeIngredientsTextView.text = formattedIngredients

            // Format and set instructions with item numbers for Firestore recipes
            val formattedSteps = recipe.instructions.mapIndexed { index, step ->
                "${index + 1}) $step"
            }.joinToString("\n\n")
            recipeInstructionsTextView.text = formattedSteps

            // Hide category and area chips for Firestore recipes (use servings and cooking time instead)
            recipeCategoryChip.visibility = View.GONE
            recipeAreaChip.visibility = View.GONE

            // Hide YouTube button for Firestore recipes
            youtubeButton.visibility = View.GONE

            // Comment section is visible for Firestore recipes
            commentsSection.visibility = View.VISIBLE
        }

        // Load recipe image using Glide
        Glide.with(this)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.image1)
            .error(R.drawable.image1)
            .into(binding.recipeImageView)
    }

    private fun setupCommentsSection() {
        // Setup RecyclerView for comments
        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.commentsRecyclerView.adapter = commentsAdapter

        // Add a new comment when the button is clicked
        binding.submitCommentButton.setOnClickListener {
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
