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

    inner class BookmarkedRecipeViewHolder(private val binding: ItemRecipeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe, context: Context) {
            binding.recipeNameTextView.text = recipe.name
            binding.recipeDescriptionTextView.text = recipe.description
            binding.recipeServingsTextView.text = "${recipe.servings}"
            binding.recipeCookingTimeTextView.text = "${recipe.cookingTime} min"
            binding.recipeUserNameTextView.text = "by ${recipe.userName}"

            Glide.with(context)
                .load(recipe.imageUrl)
                .into(binding.recipeImageView)

            // Always display the bookmark icon as filled
            binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_filled)

            // Handle unbookmarking on icon click
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkedRecipeViewHolder {
        val binding = ItemRecipeCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookmarkedRecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookmarkedRecipeViewHolder, position: Int) {
        holder.bind(bookmarkedRecipes[position], holder.itemView.context)
    }

    override fun getItemCount(): Int = bookmarkedRecipes.size

    fun updateBookmarkedRecipes(newRecipes: List<Recipe>) {
        bookmarkedRecipes.clear()
        bookmarkedRecipes.addAll(newRecipes)
        notifyDataSetChanged()
    }

    fun removeRecipe(recipe: Recipe) {
        val index = bookmarkedRecipes.indexOfFirst { it.id == recipe.id }
        if (index != -1) {
            bookmarkedRecipes.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
