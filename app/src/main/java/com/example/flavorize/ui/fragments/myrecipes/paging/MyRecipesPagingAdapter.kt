package com.example.flavorize.ui.fragments.myrecipes.paging

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flavorize.R
import com.example.flavorize.data.Recipe
import com.example.flavorize.databinding.DialogMyrecipesOptionsBinding
import com.example.flavorize.databinding.ItemRecipeCardBinding
import com.example.flavorize.ui.activities.recipedetail.RecipeDetailActivity

class MyRecipesPagingAdapter(
    private val onEditRecipe: (Recipe) -> Unit,
    private val onDeleteRecipe: (Recipe) -> Unit,
    private val onBookmarkToggle: (Recipe, Boolean, (Boolean) -> Unit) -> Unit
) : PagingDataAdapter<Recipe, MyRecipesPagingAdapter.RecipeViewHolder>(RecipeDiffCallback) {

    // ViewHolder to bind recipe data to the UI
    inner class RecipeViewHolder(private val binding: ItemRecipeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Bind recipe data to the UI components
        fun bind(recipe: Recipe) {
            binding.recipeNameTextView.text = recipe.name
            binding.recipeDescriptionTextView.text = recipe.description
            binding.recipeServingsTextView.text = recipe.servings.toString()
            binding.recipeCookingTimeTextView.text = "${recipe.cookingTime} min"
            binding.recipeUserNameTextView.text = recipe.userName

            // Load recipe image using Glide
            Glide.with(binding.root.context)
                .load(recipe.imageUrl)
                .into(binding.recipeImageView)

            // Set bookmark icon based on the recipe state
            updateBookmarkIcon(recipe.isBookmarked)

            // Handle bookmark icon click
            binding.bookmarkIcon.setOnClickListener {
                val newBookmarkStatus = !recipe.isBookmarked
                updateBookmarkIcon(newBookmarkStatus)
                onBookmarkToggle(recipe, newBookmarkStatus) { success ->
                    if (!success) {
                        // Revert the icon if bookmark toggle fails
                        updateBookmarkIcon(recipe.isBookmarked)
                    } else {
                        recipe.isBookmarked = newBookmarkStatus
                        notifyItemChanged(bindingAdapterPosition)
                    }
                }
            }

            // Handle root click to navigate to RecipeDetailActivity
            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, RecipeDetailActivity::class.java).apply {
                    putExtra("recipe", recipe)
                }
                context.startActivity(intent)
            }

            // Handle long click to show options dialog
            binding.root.setOnLongClickListener {
                showOptionsDialog(recipe)
                true
            }
        }

        // Show a dialog with options for edit and delete using ViewBinding
        private fun showOptionsDialog(recipe: Recipe) {
            // Inflate custom view with ViewBinding
            val dialogBinding = DialogMyrecipesOptionsBinding.inflate(
                LayoutInflater.from(binding.root.context)
            )

            // Create the AlertDialog instance
            val dialog = AlertDialog.Builder(binding.root.context)
                .setView(dialogBinding.root)
                .create()

            // Set click listeners for buttons
            dialogBinding.editOptionButton.setOnClickListener {
                onEditRecipe(recipe) // Call edit function
                dialog.dismiss() // Dismiss dialog after action
            }

            dialogBinding.deleteOptionButton.setOnClickListener {
                onDeleteRecipe(recipe) // Call delete function
                dialog.dismiss() // Dismiss dialog after action
            }

            // Apply rounded corners to the dialog
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }

        // Update bookmark icon based on the state
        private fun updateBookmarkIcon(isBookmarked: Boolean) {
            val bookmarkIconRes = if (isBookmarked) {
                R.drawable.ic_bookmark_filled
            } else {
                R.drawable.ic_bookmark_border
            }
            binding.bookmarkIcon.setImageResource(bookmarkIconRes)
        }
    }

    // Create a new ViewHolder for the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    // Bind the recipe data to the ViewHolder
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    // DiffUtil to optimize the list updates
    companion object {
        private val RecipeDiffCallback = object : DiffUtil.ItemCallback<Recipe>() {
            override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
                // Check if items are the same based on their ID
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
                // Check if the contents of items are the same
                return oldItem == newItem
            }
        }
    }
}
