package com.example.flavorize.ui.activities.editrecipe

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.flavorize.data.Recipe
import com.example.flavorize.databinding.ActivityEditRecipeBinding
import com.example.flavorize.ui.activities.editrecipe.viewmodel.EditRecipeViewModel

class EditRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditRecipeBinding
    private var imageUri: Uri? = null
    private lateinit var currentRecipe: Recipe

    // ViewModel for managing LiveData and Firestore operations
    private val viewModel: EditRecipeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Receive Recipe data passed from MyRecipesFragment
        currentRecipe = intent.getParcelableExtra("recipe")
            ?: throw IllegalArgumentException("Recipe data missing")

        // Pass the recipe to ViewModel
        viewModel.setRecipe(currentRecipe)

        setupToolbar() // Setup toolbar
        setupUIListeners() // Setup listeners for UI interactions
        setupObservers() // Observe ViewModel LiveData
    }

    private fun setupToolbar() {
        // Configure the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Edit Recipe"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupUIListeners() {
        // Handle image selection
        binding.recipeImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        // Add ingredient field
        binding.addIngredientButton.setOnClickListener {
            addIngredientField("")
        }

        // Add instruction field
        binding.addInstructionButton.setOnClickListener {
            addInstructionField("")
        }

        // Handle recipe submission
        binding.submitRecipeButton.setOnClickListener {
            val updatedRecipe = getUpdatedRecipe()
            if (updatedRecipe != null) {
                viewModel.updateRecipe(updatedRecipe) // Update the recipe via ViewModel
            } else {
                Toast.makeText(this, "Please complete all fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // Handle image selection result
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            binding.recipeImageView.setImageURI(imageUri)
        }
    }

    private fun addIngredientField(ingredient: String) {
        // Dynamically add ingredient input field
        val ingredientEditText = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setText(ingredient)
            hint = "Ingredient"
        }
        val addButtonIndex = binding.recipeIngredientsLayout.indexOfChild(binding.addIngredientButton)
        binding.recipeIngredientsLayout.addView(ingredientEditText, addButtonIndex)
    }

    private fun addInstructionField(instruction: String) {
        // Dynamically add instruction input field
        val instructionEditText = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setText(instruction)
            hint = "Instruction"
        }
        val addButtonIndex = binding.recipeInstructionsLayout.indexOfChild(binding.addInstructionButton)
        binding.recipeInstructionsLayout.addView(instructionEditText, addButtonIndex)
    }

    private fun getUpdatedRecipe(): Recipe? {
        // Extract updated recipe details from the form
        val name = binding.recipeNameEditText.text.toString()
        val description = binding.recipeDescriptionEditText.text.toString()
        val servings = binding.recipePortionsEditText.text.toString().toIntOrNull() ?: 0
        val cookingTime = binding.recipeCookingTimeEditText.text.toString()

        val ingredients = mutableListOf<String>()
        for (i in 0 until binding.recipeIngredientsLayout.childCount) {
            val view = binding.recipeIngredientsLayout.getChildAt(i)
            if (view is EditText) {
                val ingredient = view.text.toString()
                if (ingredient.isNotBlank()) ingredients.add(ingredient)
            }
        }

        val instructions = mutableListOf<String>()
        for (i in 0 until binding.recipeInstructionsLayout.childCount) {
            val view = binding.recipeInstructionsLayout.getChildAt(i)
            if (view is EditText) {
                val instruction = view.text.toString()
                if (instruction.isNotBlank()) instructions.add(instruction)
            }
        }

        return if (name.isNotBlank() && description.isNotBlank()) {
            currentRecipe.copy(
                name = name,
                description = description,
                servings = servings,
                cookingTime = cookingTime,
                ingredients = ingredients,
                instructions = instructions,
                imageUrl = currentRecipe.imageUrl // Update imageUrl if imageUri is set
            )
        } else null
    }

    private fun setupObservers() {
        // Observe recipe LiveData and populate UI
        viewModel.recipe.observe(this) { recipe ->
            populateUI(recipe)
        }

        // Observe update result LiveData
        viewModel.updateResult.observe(this) { result ->
            if (result.isSuccess) {
                Toast.makeText(this, "Recipe updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to update recipe: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateUI(recipe: Recipe) {
        // Populate form with recipe details
        binding.recipeNameEditText.setText(recipe.name)
        binding.recipeDescriptionEditText.setText(recipe.description)
        binding.recipePortionsEditText.setText(recipe.servings.toString())
        binding.recipeCookingTimeEditText.setText(recipe.cookingTime)

        recipe.ingredients.forEach { ingredient ->
            addIngredientField(ingredient)
        }

        recipe.instructions.forEach { instruction ->
            addInstructionField(instruction)
        }

        Glide.with(this)
            .load(recipe.imageUrl)
            .into(binding.recipeImageView)
    }
}
