package com.example.flavorize.ui.activities.editrecipe

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.flavorize.R
import com.example.flavorize.data.Recipe
import com.example.flavorize.databinding.ActivityEditRecipeBinding
import com.example.flavorize.ui.activities.editrecipe.viewmodel.EditRecipeViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

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
        @Suppress("DEPRECATION")
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
            addIngredientField(binding.recipeIngredientsLayout, binding.addIngredientButton, "")
        }

        // Add instruction field
        binding.addInstructionButton.setOnClickListener {
            addInstructionField(binding.recipeInstructionsLayout, binding.addInstructionButton, "")
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

    private fun addIngredientField(parentLayout: LinearLayout, addButton: View, initialText: String = "") {
        // Create a container for the field and remove button
        val container = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            orientation = LinearLayout.HORIZONTAL
        }

        // Create a styled ingredient input field
        val inputLayout = TextInputLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            ).apply {
                marginEnd = 8
            }
            boxBackgroundColor = 0
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            setBoxStrokeColorStateList(resources.getColorStateList(android.R.color.holo_orange_light, theme))
            setHintTextColor(resources.getColorStateList(android.R.color.holo_orange_light, theme))
        }

        val ingredientEditText = TextInputEditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setText(initialText.trim()) // Trim whitespace or newline
            hint = "Ingredient"
            imeOptions = EditorInfo.IME_ACTION_DONE
            setSingleLine(true)
        }

        inputLayout.addView(ingredientEditText)

        // Create delete button
        val removeButton = ImageButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                48,
                48
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
            setImageResource(com.example.flavorize.R.drawable.ic_close)
            setBackgroundResource(0)
            contentDescription = "Remove ingredient"
            setOnClickListener {
                parentLayout.removeView(container)
            }
        }

        // Add views to container
        container.addView(inputLayout)
        container.addView(removeButton)

        // Add the container above the button
        val buttonIndex = parentLayout.indexOfChild(addButton)
        parentLayout.addView(container, buttonIndex)
    }

    private fun addInstructionField(parentLayout: LinearLayout, addButton: View, initialText: String = "") {
        // Create a container for the field and remove button
        val container = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            orientation = LinearLayout.HORIZONTAL
        }

        // Create a styled instruction input field
        val inputLayout = TextInputLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            ).apply {
                marginEnd = 8
            }
            boxBackgroundColor = 0
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            setBoxStrokeColorStateList(resources.getColorStateList(android.R.color.holo_orange_light, theme))
            setHintTextColor(resources.getColorStateList(android.R.color.holo_orange_light, theme))
        }

        val instructionEditText = TextInputEditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setText(initialText.trim()) // Trim whitespace or newline
            hint = "Instruction"
            imeOptions = EditorInfo.IME_ACTION_DONE
            setSingleLine(true)
        }

        inputLayout.addView(instructionEditText)

        // Create delete button
        val removeButton = ImageButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                48,
                48
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
            setImageResource(com.example.flavorize.R.drawable.ic_close)
            setBackgroundResource(0)
            contentDescription = "Remove instruction"
            setOnClickListener {
                parentLayout.removeView(container)
            }
        }

        // Add views to container
        container.addView(inputLayout)
        container.addView(removeButton)

        // Add the container above the button
        val buttonIndex = parentLayout.indexOfChild(addButton)
        parentLayout.addView(container, buttonIndex)
    }

    private fun getUpdatedRecipe(): Recipe? {
        // Extract updated recipe details from the form
        val name = binding.recipeNameEditText.text.toString().trim()
        val description = binding.recipeDescriptionEditText.text.toString().trim()
        val servings = binding.recipePortionsEditText.text.toString().toIntOrNull() ?: 0
        val cookingTime = binding.recipeCookingTimeEditText.text.toString().trim()

        // Clean and filter ingredients
        val ingredients = mutableListOf<String>()
        for (i in 0 until binding.recipeIngredientsLayout.childCount) {
            val view = binding.recipeIngredientsLayout.getChildAt(i)
            if (view is LinearLayout) {
                // Find the TextInputLayout in the container
                val textInputLayout = view.getChildAt(0) as? TextInputLayout
                textInputLayout?.editText?.let { editText ->
                    val ingredient = editText.text.toString().trim()
                    if (ingredient.isNotBlank()) { // Only add non-blank ingredients
                        ingredients.add(ingredient)
                    }
                }
            }
        }

        // Clean and filter instructions
        val instructions = mutableListOf<String>()
        for (i in 0 until binding.recipeInstructionsLayout.childCount) {
            val view = binding.recipeInstructionsLayout.getChildAt(i)
            if (view is LinearLayout) {
                // Find the TextInputLayout in the container
                val textInputLayout = view.getChildAt(0) as? TextInputLayout
                textInputLayout?.editText?.let { editText ->
                    val instruction = editText.text.toString().trim()
                    if (instruction.isNotBlank()) { // Only add non-blank instructions
                        instructions.add(instruction)
                    }
                }
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
        binding.recipeNameEditText.setText(recipe.name.trim())
        binding.recipeDescriptionEditText.setText(recipe.description.trim())
        binding.recipePortionsEditText.setText(recipe.servings.toString())
        binding.recipeCookingTimeEditText.setText(recipe.cookingTime.trim())

        // Populate ingredients and instructions
        recipe.ingredients.forEach { ingredient ->
            addIngredientField(binding.recipeIngredientsLayout, binding.addIngredientButton, ingredient.trim())
        }

        recipe.instructions.forEach { instruction ->
            addInstructionField(binding.recipeInstructionsLayout, binding.addInstructionButton, instruction.trim())
        }

        Glide.with(this)
            .load(recipe.imageUrl)
            .into(binding.recipeImageView)
    }
}
