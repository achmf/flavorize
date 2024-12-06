package com.example.flavorize.ui.fragments.recipes.paging

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flavorize.R
import com.example.flavorize.data.Recipe
import com.example.flavorize.databinding.ItemRecipeCardBinding
import com.example.flavorize.ui.activities.recipedetail.RecipeDetailActivity

class RecipesPagingAdapter(
    private val onBookmarkToggle: (Recipe, Boolean, (Boolean) -> Unit) -> Unit
) : PagingDataAdapter<Recipe, RecipesPagingAdapter.RecipeViewHolder>(RecipeDiffCallback) {

    // ViewHolder class to bind recipe data to the UI
    inner class RecipeViewHolder(private val binding: ItemRecipeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Bind recipe data to the UI components
        fun bind(recipe: Recipe) {
            // Set recipe details to respective TextViews
            binding.recipeNameTextView.text = recipe.name
            binding.recipeDescriptionTextView.text = recipe.description
            binding.recipeServingsTextView.text = recipe.servings.toString()
            binding.recipeCookingTimeTextView.text = "${recipe.cookingTime} min"
            binding.recipeUserNameTextView.text = recipe.userName

            // Load recipe image using Glide
            Glide.with(binding.root.context)
                .load(recipe.imageUrl)
                .into(binding.recipeImageView)

            // Set the bookmark icon based on the state
            updateBookmarkIcon(recipe.isBookmarked)

            // Handle bookmark icon click
            binding.bookmarkIcon.setOnClickListener {
                val newBookmarkStatus = !recipe.isBookmarked
                updateBookmarkIcon(newBookmarkStatus)
                onBookmarkToggle(recipe, newBookmarkStatus) { success ->
                    if (!success) {
                        // Revert icon if toggle fails
                        updateBookmarkIcon(recipe.isBookmarked)
                    } else {
                        // Update recipe state if toggle succeeds
                        recipe.isBookmarked = newBookmarkStatus
                        notifyItemChanged(bindingAdapterPosition)
                    }
                }
            }

            // Navigate to RecipeDetailActivity on item click
            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, RecipeDetailActivity::class.java).apply {
                    putExtra("recipe", recipe)
                }
                context.startActivity(intent)
            }
        }

        // Update the bookmark icon based on the current state
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

    // Define DiffUtil for optimized updates to the list
    companion object {
        private val RecipeDiffCallback = object : DiffUtil.ItemCallback<Recipe>() {
            // Check if items are the same by comparing their IDs
            override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
                return oldItem.id == newItem.id
            }

            // Check if the content of items is the same
            override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
                return oldItem == newItem
            }
        }
    }
}
