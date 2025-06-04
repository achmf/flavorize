package com.example.flavorize.ui.fragments.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flavorize.R
import com.example.flavorize.data.api.MealDbRecipe

/**
 * Adapter for displaying MealDbRecipe items in a RecyclerView
 */
class RecipeCardAdapter(
    private val onItemClick: (MealDbRecipe) -> Unit
) : ListAdapter<MealDbRecipe, RecipeCardAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_card, parent, false)
        return RecipeViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RecipeViewHolder(
        itemView: View,
        private val onItemClick: (MealDbRecipe) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.recipeImage)
        private val nameTextView: TextView = itemView.findViewById(R.id.recipeName)
        private val categoryTextView: TextView = itemView.findViewById(R.id.recipeCategory)
        private val areaTextView: TextView = itemView.findViewById(R.id.recipeArea)
        private val ingredientsTextView: TextView = itemView.findViewById(R.id.recipeIngredients)

        private var currentRecipe: MealDbRecipe? = null

        init {
            itemView.setOnClickListener {
                currentRecipe?.let { recipe ->
                    onItemClick(recipe)
                }
            }
        }

        fun bind(recipe: MealDbRecipe) {
            currentRecipe = recipe

            // Set recipe name
            nameTextView.text = recipe.name

            // Set category and area
            categoryTextView.text = recipe.category
            areaTextView.text = recipe.area

            // Format and set ingredients (limited to first 5)
            val ingredientsText = recipe.ingredients
                .take(5)
                .joinToString(", ")
            ingredientsTextView.text = ingredientsText

            // Load image using Glide
            Glide.with(itemView.context)
                .load(recipe.imageUrl)
                .placeholder(R.drawable.image1) // Use a placeholder image
                .error(R.drawable.image1) // Use an error image
                .into(imageView)
        }
    }

    /**
     * DiffUtil callback for efficient RecyclerView updates
     */
    class RecipeDiffCallback : DiffUtil.ItemCallback<MealDbRecipe>() {
        override fun areItemsTheSame(oldItem: MealDbRecipe, newItem: MealDbRecipe): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MealDbRecipe, newItem: MealDbRecipe): Boolean {
            return oldItem == newItem
        }
    }
}
