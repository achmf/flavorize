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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.data.Recipe
import com.example.flavorize.databinding.ActivityEditRecipeBinding
import kotlinx.coroutines.launch

class EditRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditRecipeBinding
    private var imageUri: Uri? = null
    private lateinit var currentRecipe: Recipe
    private val firestoreRepository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Receive Recipe data passed from MyRecipesFragment
        currentRecipe = intent.getParcelableExtra("recipe")
            ?: throw IllegalArgumentException("Recipe data missing")

        setupToolbar()
        populateUI()
        setupUIListeners()
    }

    private fun setupToolbar() {
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

    private fun populateUI() {
        binding.recipeNameEditText.setText(currentRecipe.name)
        binding.recipeDescriptionEditText.setText(currentRecipe.description)
        binding.recipePortionsEditText.setText(currentRecipe.servings.toString())
        binding.recipeCookingTimeEditText.setText(currentRecipe.cookingTime)

        // Populate ingredients
        currentRecipe.ingredients.forEach { ingredient ->
            addIngredientField(ingredient)
        }

        // Populate instructions
        currentRecipe.instructions.forEach { instruction ->
            addInstructionField(instruction)
        }

        // Load image
        Glide.with(this)
            .load(currentRecipe.imageUrl)
            .into(binding.recipeImageView)
    }

    private fun setupUIListeners() {
        binding.recipeImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        binding.addIngredientButton.setOnClickListener {
            addIngredientField("")
        }

        binding.addInstructionButton.setOnClickListener {
            addInstructionField("")
        }

        binding.submitRecipeButton.setOnClickListener {
            val updatedRecipe = getUpdatedRecipe()
            if (updatedRecipe != null) {
                updateRecipeInFirestore(updatedRecipe)
            } else {
                Toast.makeText(this, "Please complete all fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            binding.recipeImageView.setImageURI(imageUri)
        }
    }

    private fun addIngredientField(ingredient: String) {
        val ingredientEditText = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setText(ingredient)
            hint = "Ingredient"
        }
        // Insert ingredient field above the Add button
        val addButtonIndex = binding.recipeIngredientsLayout.indexOfChild(binding.addIngredientButton)
        binding.recipeIngredientsLayout.addView(ingredientEditText, addButtonIndex)
    }

    private fun addInstructionField(instruction: String) {
        val instructionEditText = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setText(instruction)
            hint = "Instruction"
        }
        // Insert instruction field above the Add button
        val addButtonIndex = binding.recipeInstructionsLayout.indexOfChild(binding.addInstructionButton)
        binding.recipeInstructionsLayout.addView(instructionEditText, addButtonIndex)
    }

    private fun getUpdatedRecipe(): Recipe? {
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
                imageUrl = currentRecipe.imageUrl // Image URL would need to be updated if imageUri is new
            )
        } else null
    }

    private fun updateRecipeInFirestore(updatedRecipe: Recipe) {
        lifecycleScope.launch {
            val result = firestoreRepository.updateRecipe(updatedRecipe.id, updatedRecipe)
            if (result.isSuccess) {
                Toast.makeText(this@EditRecipeActivity, "Recipe updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@EditRecipeActivity, "Failed to update recipe: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
