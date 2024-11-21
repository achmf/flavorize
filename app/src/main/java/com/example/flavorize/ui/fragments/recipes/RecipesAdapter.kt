package com.example.flavorize.ui.fragments.recipes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flavorize.data.Recipe
import com.example.flavorize.databinding.ItemRecipeCardBinding

class RecipesAdapter(var allRecipes: List<Recipe>) : RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(private val binding: ItemRecipeCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: Recipe) {
            binding.recipeNameTextView.text = recipe.name
            binding.recipeDescriptionTextView.text = recipe.description
            binding.recipeServingsTextView.text = "${recipe.servings}"
            binding.recipeCookingTimeTextView.text = "${recipe.cookingTime} min"
            binding.recipeUserNameTextView.text = "by ${recipe.userName}"

            // Load image using Glide
            Glide.with(binding.root.context)
                .load(recipe.imageUrl)
                .into(binding.recipeImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(allRecipes[position])
    }

    override fun getItemCount(): Int {
        return allRecipes.size
    }

    fun updateRecipes(newRecipes: List<Recipe>) {
        allRecipes = newRecipes
        notifyDataSetChanged()
    }
}