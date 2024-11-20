package com.example.flavorize.ui.createform

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.flavorize.R
import com.example.flavorize.data.Recipe
import com.example.flavorize.databinding.ActivityCreateRecipeFormBinding
import com.example.flavorize.ui.createform.viewmodel.CreateRecipeViewModel

class CreateRecipeFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateRecipeFormBinding
    private val viewModel: CreateRecipeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateRecipeFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val addIngredientButton: Button = findViewById(R.id.addIngredientButton)
        val recipeIngredientsLayout: LinearLayout = findViewById(R.id.recipeIngredientsLayout)

        addIngredientButton.setOnClickListener {
            val ingredientEditText = EditText(this)
            ingredientEditText.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            ingredientEditText.hint = "Ingredient"
            recipeIngredientsLayout.addView(ingredientEditText, recipeIngredientsLayout.childCount - 1)
        }

        val addInstructionButton: Button = findViewById(R.id.addInstructionButton)
        val recipeInstructionsLayout: LinearLayout = findViewById(R.id.recipeInstructionsLayout)

        addInstructionButton.setOnClickListener {
            val instructionEditText = EditText(this)
            instructionEditText.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            instructionEditText.hint = "Instruction"
            recipeInstructionsLayout.addView(instructionEditText, recipeInstructionsLayout.childCount - 1)
        }

        binding.submitRecipeButton.setOnClickListener {
            val recipeName = binding.recipeNameEditText.text.toString()
            val recipeDescription = binding.recipeDescriptionEditText.text.toString()
            val recipePortions = binding.recipePortionsEditText.text.toString().toIntOrNull() ?: 0
            val recipeCookingTime = binding.recipeCookingTimeEditText.text.toString()

            val ingredients = mutableListOf<String>()
            for (i in 0 until recipeIngredientsLayout.childCount) {
                val view = recipeIngredientsLayout.getChildAt(i)
                if (view is EditText) {
                    val ingredient = view.text.toString()
                    if (ingredient.isNotBlank()) {
                        ingredients.add(ingredient)
                    }
                }
            }

            val instructions = mutableListOf<String>()
            for (i in 0 until recipeInstructionsLayout.childCount) {
                val view = recipeInstructionsLayout.getChildAt(i)
                if (view is EditText) {
                    val instruction = view.text.toString()
                    if (instruction.isNotBlank()) {
                        instructions.add(instruction)
                    }
                }
            }

            val recipe = Recipe(
                name = recipeName,
                description = recipeDescription,
                servings = recipePortions,
                cookingTime = recipeCookingTime,
                ingredients = ingredients,
                instructions = instructions
            )

            viewModel.addRecipe(recipe)
        }

        viewModel.addRecipeResult.observe(this) { result ->
            if (result.isSuccess) {
                Toast.makeText(this, "Recipe added successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Failed to add recipe: ${result.exceptionOrNull()?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}