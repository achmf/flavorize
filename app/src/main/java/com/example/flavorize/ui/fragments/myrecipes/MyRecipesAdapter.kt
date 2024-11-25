package com.example.flavorize.ui.fragments.myrecipes

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flavorize.ui.activities.editrecipe.EditRecipeActivity
import com.example.flavorize.data.Recipe
import com.example.flavorize.databinding.ItemRecipeCardBinding
import com.example.flavorize.ui.activities.recipedetail.RecipeDetailActivity

class MyRecipesAdapter(
    private var allRecipes: List<Recipe>,
    private val onDelete: (Recipe) -> Unit
) : RecyclerView.Adapter<MyRecipesAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(private val binding: ItemRecipeCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: Recipe, context: Context) {
            binding.recipeNameTextView.text = recipe.name
            binding.recipeDescriptionTextView.text = recipe.description
            binding.recipeServingsTextView.text = "${recipe.servings}"
            binding.recipeCookingTimeTextView.text = "${recipe.cookingTime} min"
//            binding.recipeUserNameTextView.text = "by ${recipe.userName}"

            // Hide bookmark icon for MyRecipesAdapter
            binding.bookmarkIcon.visibility = View.GONE

            // Load image using Glide
            Glide.with(context)
                .load(recipe.imageUrl)
                .into(binding.recipeImageView)

            // Set onClickListener to show dialog for Edit/Delete
            binding.root.setOnClickListener {
                showRecipeOptionsDialog(context, recipe)
            }
        }

        private fun showRecipeOptionsDialog(context: Context, recipe: Recipe) {
            val options = arrayOf("View Recipe", "Edit", "Delete")
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Select an Action")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> navigateToViewRecipe(context, recipe) // View Recipe
                    1 -> navigateToEditRecipe(context, recipe) // Edit Recipe
                    2 -> onDelete(recipe) // Delete Recipe
                }
            }
            builder.show()
        }

        private fun navigateToViewRecipe(context: Context, recipe: Recipe) {
            val intent = Intent(context, RecipeDetailActivity::class.java)
            intent.putExtra("recipe", recipe)
            context.startActivity(intent)
        }

        private fun navigateToEditRecipe(context: Context, recipe: Recipe) {
            val intent = Intent(context, EditRecipeActivity::class.java)
            intent.putExtra("recipe", recipe)
            context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(allRecipes[position], holder.itemView.context)
    }

    override fun getItemCount(): Int {
        return allRecipes.size
    }

    fun updateRecipes(newRecipes: List<Recipe>) {
        allRecipes = newRecipes
        notifyDataSetChanged()
    }
}
