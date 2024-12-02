package com.example.flavorize.ui.activities.recipedetail

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.flavorize.R
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.data.Recipe
import com.example.flavorize.data.RecipeComment
import com.example.flavorize.databinding.ActivityRecipeDetailBinding
import kotlinx.coroutines.launch

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailBinding
    private val firestoreRepository = FirestoreRepository()
    private val commentsAdapter = RecipeCommentsAdapter(
        currentUserId = firestoreRepository.getCurrentUserId() ?: "", // Ambil ID user saat ini
        onCommentLongClick = { comment, view ->
            showPopupMenu(comment, view) // Tampilkan popup menu saat komentar ditekan lama
        }
    )
    private var recipe: Recipe? = null // Store the recipe object

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupSwipeToRefresh()

        // Ambil data Recipe yang diteruskan melalui Intent
        recipe = intent.getParcelableExtra("recipe")
        recipe?.let {
            bindRecipeDetails(it)
            setupCommentsSection()
        } ?: run {
            Toast.makeText(this, "Recipe data not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Back button
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            recipe?.let {
                fetchComments(it.id)
            }
        }
    }

    private fun bindRecipeDetails(recipe: Recipe) {
        lifecycleScope.launch {
            val userName = FirestoreRepository().getUserNameById(recipe.userId) // Ambil nama user
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

        Glide.with(this)
            .load(recipe.imageUrl)
            .into(binding.recipeImageView)
    }

    private fun setupCommentsSection() {
        // Set up RecyclerView for comments
        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.commentsRecyclerView.adapter = commentsAdapter

        // Ambil komentar untuk resep ini
        recipe?.let {
            fetchComments(it.id)
        }

        // Set up "Add Comment" button
        binding.addCommentButton.setOnClickListener {
            val commentText = binding.commentEditText.text.toString().trim()
            if (commentText.isNotEmpty()) {
                postComment(commentText)
                binding.commentEditText.text.clear() // Clear the input field
            } else {
                Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchComments(recipeId: String) {
        lifecycleScope.launch {
            val result = firestoreRepository.getCommentsForRecipe(recipeId)
            if (result.isSuccess) {
                commentsAdapter.submitList(result.getOrDefault(emptyList()))
            } else {
                Toast.makeText(this@RecipeDetailActivity, "Failed to load comments", Toast.LENGTH_SHORT).show()
            }
            // Nonaktifkan SwipeRefreshLayout setelah selesai memuat data
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun postComment(commentText: String) {
        val userId = firestoreRepository.getCurrentUserId() ?: return
        val userName = firestoreRepository.getCurrentUserName()

        val newComment = RecipeComment(
            recipeId = recipe!!.id,
            userId = userId,
            userName = userName,
            text = commentText,
            timestamp = System.currentTimeMillis()
        )

        lifecycleScope.launch {
            val result = firestoreRepository.addCommentToRecipe(recipe!!.id, newComment)
            if (result.isSuccess) {
                Toast.makeText(this@RecipeDetailActivity, "Comment added", Toast.LENGTH_SHORT).show()
                // Fetch comments again after adding a new one
                fetchComments(recipe!!.id)
            } else {
                Toast.makeText(this@RecipeDetailActivity, "Failed to add comment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPopupMenu(comment: RecipeComment, anchorView: View) {
        val popupMenu = PopupMenu(this, anchorView)
        popupMenu.inflate(R.menu.comment_popup_menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    showEditCommentDialog(comment)
                    true
                }
                R.id.action_delete -> {
                    deleteComment(comment)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showEditCommentDialog(comment: RecipeComment) {
        val editText = androidx.appcompat.widget.AppCompatEditText(this)
        editText.setText(comment.text)
        AlertDialog.Builder(this)
            .setTitle("Edit Comment")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val updatedText = editText.text.toString()
                if (updatedText.isNotEmpty()) {
                    updateComment(comment.copy(text = updatedText))
                } else {
                    Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateComment(comment: RecipeComment) {
        lifecycleScope.launch {
            val result = firestoreRepository.updateComment(comment)
            if (result.isSuccess) {
                fetchComments(comment.recipeId)
                Toast.makeText(this@RecipeDetailActivity, "Comment updated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@RecipeDetailActivity, "Failed to update comment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteComment(comment: RecipeComment) {
        // Tampilkan popup konfirmasi
        AlertDialog.Builder(this)
            .setTitle("Delete Comment")
            .setMessage("Are you sure you want to delete this comment?")
            .setPositiveButton("Yes") { _, _ ->
                // Jika pengguna mengkonfirmasi penghapusan
                lifecycleScope.launch {
                    val result = firestoreRepository.deleteComment(comment)
                    if (result.isSuccess) {
                        fetchComments(comment.recipeId) // Refresh komentar setelah penghapusan
                        Toast.makeText(this@RecipeDetailActivity, "Comment deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@RecipeDetailActivity, "Failed to delete comment", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("No", null) // Jika pengguna membatalkan, tidak ada tindakan
            .show()
    }

}
