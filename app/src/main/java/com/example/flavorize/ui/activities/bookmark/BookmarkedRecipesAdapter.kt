package com.example.flavorize.ui.activities.bookmark

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
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
            binding.recipeNameTextView.text = recipe.name
            binding.recipeDescriptionTextView.text = recipe.description
            binding.recipeServingsTextView.text = recipe.servings.toString()
            binding.recipeCookingTimeTextView.text = "${recipe.cookingTime} min"
            binding.recipeUserNameTextView.text = recipe.userName

            // Load recipe image using Glide
            Glide.with(context)
                .load(recipe.imageUrl)
                .into(binding.recipeImageView)

            // Set bookmark icon as filled
            binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_filled)

            // Handle unbookmark action
            binding.bookmarkIcon.setOnClickListener {
                onBookmarkToggle(recipe)
            }

            // Open recipe details on card click
            binding.root.setOnClickListener {
                val intent = Intent(context, RecipeDetailActivity::class.java)
                intent.putExtra("recipe", recipe)
                context.startActivity(intent)
            }
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

    // Update the adapter with new recipe data
    fun updateBookmarkedRecipes(newRecipes: List<Recipe>) {
        bookmarkedRecipes.clear()
        bookmarkedRecipes.addAll(newRecipes)
        notifyDataSetChanged()
    }
}
