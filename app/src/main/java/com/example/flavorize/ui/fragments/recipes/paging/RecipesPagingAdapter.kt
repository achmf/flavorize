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
    private val onBookmarkToggle: (Recipe, Boolean) -> Unit // Callback untuk bookmark toggle
) : PagingDataAdapter<Recipe, RecipesPagingAdapter.RecipeViewHolder>(RecipeDiffCallback) {

    inner class RecipeViewHolder(private val binding: ItemRecipeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            binding.recipeNameTextView.text = recipe.name
            binding.recipeDescriptionTextView.text = recipe.description
            binding.recipeServingsTextView.text = recipe.servings.toString()
            binding.recipeCookingTimeTextView.text = binding.root.context.getString(
                R.string.recipe_cooking_time_min, recipe.cookingTime
            )

            Glide.with(binding.root.context)
                .load(recipe.imageUrl)
                .into(binding.recipeImageView)

            // Set bookmark icon based on current status
            val bookmarkIconRes = if (recipe.isBookmarked) {
                R.drawable.ic_bookmark_filled
            } else {
                R.drawable.ic_bookmark_border
            }
            binding.bookmarkIcon.setImageResource(bookmarkIconRes)

            // Handle bookmark toggle
            binding.bookmarkIcon.setOnClickListener {
                val newBookmarkStatus = !recipe.isBookmarked
                recipe.isBookmarked = newBookmarkStatus // Update local status
                val updatedIconRes = if (newBookmarkStatus) {
                    R.drawable.ic_bookmark_filled
                } else {
                    R.drawable.ic_bookmark_border
                }
                binding.bookmarkIcon.setImageResource(updatedIconRes)

                // Call the toggle callback to notify ViewModel/Repository
                onBookmarkToggle(recipe, newBookmarkStatus)
            }

            // Open recipe details on card click
            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, RecipeDetailActivity::class.java).apply {
                    putExtra("recipe", recipe) // Pass the Recipe object
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    companion object {
        private val RecipeDiffCallback = object : DiffUtil.ItemCallback<Recipe>() {
            override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
                return oldItem == newItem
            }
        }
    }
}
