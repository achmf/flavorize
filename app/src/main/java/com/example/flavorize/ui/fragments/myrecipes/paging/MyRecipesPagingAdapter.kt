package com.example.flavorize.ui.fragments.myrecipes.paging

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flavorize.data.Recipe
import com.example.flavorize.databinding.ItemRecipeCardBinding
import com.example.flavorize.ui.activities.recipedetail.RecipeDetailActivity

class MyRecipesPagingAdapter(
    private val onEditRecipe: (Recipe) -> Unit,
    private val onDeleteRecipe: (Recipe) -> Unit
) : PagingDataAdapter<Recipe, MyRecipesPagingAdapter.RecipeViewHolder>(RecipeDiffCallback) {

    inner class RecipeViewHolder(private val binding: ItemRecipeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            binding.recipeNameTextView.text = recipe.name
            binding.recipeDescriptionTextView.text = recipe.description
            binding.recipeServingsTextView.text = recipe.servings.toString()
            binding.recipeCookingTimeTextView.text = recipe.cookingTime
            binding.recipeUserNameTextView.text = recipe.userName

            Glide.with(binding.root.context)
                .load(recipe.imageUrl)
                .into(binding.recipeImageView)

            // Single tap untuk melihat detail
            binding.root.setOnClickListener { navigateToRecipeDetail(recipe) }

            // Long press untuk menampilkan dialog opsi
            binding.root.setOnLongClickListener {
                showOptionsDialog(recipe)
                true
            }
        }

        private fun showOptionsDialog(recipe: Recipe) {
            val options = arrayOf("Edit Recipe", "Delete Recipe") // Opsi "View Recipe" dihapus
            val dialog = AlertDialog.Builder(binding.root.context)
                .setTitle("Choose an action")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> onEditRecipe(recipe) // Edit Recipe
                        1 -> onDeleteRecipe(recipe) // Delete Recipe
                    }
                }
                .create()
            dialog.show()
        }

        private fun navigateToRecipeDetail(recipe: Recipe) {
            val context = binding.root.context
            val intent = Intent(context, RecipeDetailActivity::class.java).apply {
                putExtra("recipe", recipe)
            }
            context.startActivity(intent)
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
