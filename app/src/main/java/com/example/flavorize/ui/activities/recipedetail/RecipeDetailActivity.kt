package com.example.flavorize.ui.activities.recipedetail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.flavorize.R
import com.example.flavorize.data.Recipe
import com.example.flavorize.databinding.ActivityRecipeDetailBinding

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        // Ambil data Recipe yang diteruskan melalui Intent
        val recipe = intent.getParcelableExtra<Recipe>("recipe")
        recipe?.let { bindRecipeDetails(it) }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Back button
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun bindRecipeDetails(recipe: Recipe) {
        binding.recipeNameTextView.text = recipe.name
        binding.recipeDescriptionTextView.text = recipe.description
        binding.recipeServingsTextView.text = getString(R.string.servings_text, recipe.servings)
        binding.recipeCookingTimeTextView.text = getString(R.string.cooking_time_text, recipe.cookingTime)
        binding.recipeUserNameTextView.text = getString(R.string.created_by_text, recipe.userName)

        // Format ingredients as numbered list
        val formattedIngredients = recipe.ingredients.mapIndexed { index, ingredient ->
            "${index + 1}. $ingredient"
        }.joinToString("\n\n")
        binding.ingredientsTextView.text = formattedIngredients

        // Format steps as numbered list
        val formattedSteps = recipe.instructions.mapIndexed { index, step ->
            "${index + 1}. $step"
        }.joinToString("\n\n")
        binding.instructionsTextView.text = formattedSteps

        // Load image using Glide
        Glide.with(this)
            .load(recipe.imageUrl)
            .into(binding.recipeImageView)
    }
}
