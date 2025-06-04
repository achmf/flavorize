package com.example.flavorize.ui.activities.bookmark

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flavorize.R
import com.example.flavorize.data.Recipe
import com.example.flavorize.databinding.ItemRecipeCardBinding
import com.example.flavorize.ui.activities.recipedetail.RecipeDetailActivity

class BookmarkedRecipesAdapter(
    private var bookmarkedRecipes: MutableList<Recipe>,
    private val onBookmarkToggle: (Recipe) -> Unit
) : RecyclerView.Adapter<BookmarkedRecipesAdapter.BookmarkedRecipeViewHolder>() {

    // ViewHolder to bind recipe data to the item layout
    inner class BookmarkedRecipeViewHolder(private val binding: ItemRecipeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Bind recipe details to the UI elements
        fun bind(recipe: Recipe, context: Context) {
            // Set recipe name and ingredients
            binding.recipeName.text = recipe.name
            binding.recipeIngredients.text = recipe.ingredients.take(3).joinToString(", ")

            // Set category (using description as category) and area (using cooking time)
            binding.recipeCategory.text = recipe.description.take(20)
            binding.recipeArea.text = recipe.cookingTime

            // Load recipe image using Glide
            Glide.with(context)
                .load(recipe.imageUrl)
                .placeholder(R.drawable.image1)
                .error(R.drawable.image1)
                .into(binding.recipeImage)

            // Open recipe details on card click
            binding.root.setOnClickListener {
                val intent = Intent(context, RecipeDetailActivity::class.java)
                intent.putExtra("recipe", recipe)
                context.startActivity(intent)
            }
        }
    }

    // Create a DiffUtil callback for efficient updates
    private class RecipeDiffCallback(
        private val oldList: List<Recipe>,
        private val newList: List<Recipe>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    // Inflate the layout for the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkedRecipeViewHolder {
        val binding = ItemRecipeCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookmarkedRecipeViewHolder(binding)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: BookmarkedRecipeViewHolder, position: Int) {
        holder.bind(bookmarkedRecipes[position], holder.itemView.context)
    }

    // Return the size of the recipe list
    override fun getItemCount(): Int = bookmarkedRecipes.size

    // Update the adapter with new recipe data using DiffUtil for efficient updates
    fun updateBookmarkedRecipes(newRecipes: List<Recipe>) {
        val diffCallback = RecipeDiffCallback(bookmarkedRecipes, newRecipes)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        bookmarkedRecipes.clear()
        bookmarkedRecipes.addAll(newRecipes)

        diffResult.dispatchUpdatesTo(this)
    }
}
