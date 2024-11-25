package com.example.flavorize

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flavorize.data.Recipe
import com.example.flavorize.databinding.ItemRecipeCardBinding
import com.example.flavorize.ui.activities.recipedetail.RecipeDetailActivity

class BookmarkedRecipesAdapter(
    private var bookmarkedRecipes: MutableList<Recipe>
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

            // Disable the bookmark icon click in this activity
            binding.bookmarkIcon.setImageResource(
                if (recipe.isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_border
            )

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
}
