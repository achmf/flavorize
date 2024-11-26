package com.example.flavorize.ui.fragments.recipes

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

class RecipesAdapter(
    private var allRecipes: MutableList<Recipe>,
    private val userId: String,
    private val onBookmarkToggle: (Recipe, Boolean) -> Unit
) : RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(private val binding: ItemRecipeCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: Recipe, context: Context) {
            binding.recipeNameTextView.text = recipe.name
            binding.recipeDescriptionTextView.text = recipe.description
            binding.recipeServingsTextView.text = "${recipe.servings}"
            binding.recipeCookingTimeTextView.text = "${recipe.cookingTime} min"
            binding.recipeUserNameTextView.text = "by ${recipe.userName}"

            Glide.with(context)
                .load(recipe.imageUrl)
                .into(binding.recipeImageView)

            binding.bookmarkIcon.setImageResource(
                if (recipe.isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_border
            )

            binding.bookmarkIcon.setOnClickListener {
                val newBookmarkStatus = !recipe.isBookmarked
                onBookmarkToggle(recipe.copy(isBookmarked = newBookmarkStatus), newBookmarkStatus)

                // Optimistic UI update
                binding.bookmarkIcon.setImageResource(
                    if (newBookmarkStatus) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_border
                )
            }

            binding.root.setOnClickListener {
                val intent = Intent(context, RecipeDetailActivity::class.java)
                intent.putExtra("recipe", recipe)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(allRecipes[position], holder.itemView.context)
    }

    override fun getItemCount(): Int = allRecipes.size

    fun updateRecipes(newRecipes: List<Recipe>) {
        allRecipes.clear()
        allRecipes.addAll(newRecipes)
        notifyDataSetChanged()
    }

    fun updateRecipe(updatedRecipe: Recipe) {
        val index = allRecipes.indexOfFirst { it.id == updatedRecipe.id }
        if (index != -1) {
            allRecipes[index] = updatedRecipe
            notifyItemChanged(index)
        }
    }
}
