package com.example.flavorize.ui.createform

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.flavorize.R
import com.example.flavorize.databinding.ActivityCreateRecipeFormBinding

class CreateRecipeFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateRecipeFormBinding

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
    }
}