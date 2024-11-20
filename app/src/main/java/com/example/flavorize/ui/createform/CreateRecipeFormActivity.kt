package com.example.flavorize.ui.createform

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.flavorize.R
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.data.Recipe
import com.example.flavorize.databinding.ActivityCreateRecipeFormBinding
import com.example.flavorize.ui.createform.viewmodel.CreateRecipeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.UUID

class CreateRecipeFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateRecipeFormBinding
    private val viewModel: CreateRecipeViewModel by viewModels()
    private var imageUri: Uri? = null

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

        val recipeImageView: ImageView = findViewById(R.id.recipeImageView)
        recipeImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
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

            if (imageUri != null) {
                uploadImageAndSaveRecipe(recipeName, recipeDescription, recipePortions, recipeCookingTime, ingredients, instructions)
            } else {
                Toast.makeText(this, "Please select an image for the recipe", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.addRecipeResult.observe(this, Observer { result ->
            if (result.isSuccess) {
                Toast.makeText(this, "Recipe added successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to add recipe: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            binding.recipeImageView.setImageURI(imageUri)
        }
    }

    private fun uploadImageAndSaveRecipe(name: String, description: String, servings: Int, cookingTime: String, ingredients: List<String>, instructions: List<String>) {
        imageUri?.let { uri ->
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            inputStream?.let {
                val imageData = it.readBytes()
                val imageName = UUID.randomUUID().toString()
                CoroutineScope(Dispatchers.IO).launch {
                    val response = FirestoreRepository().uploadRecipeImage(imageData, imageName)
                    if (response.isSuccess) {
                        val imageUrl = response.getOrNull()
                        if (imageUrl != null) {
                            val recipe = Recipe(
                                name = name,
                                description = description,
                                servings = servings,
                                cookingTime = cookingTime,
                                ingredients = ingredients,
                                instructions = instructions,
                                imageUrl = imageUrl
                            )
                            viewModel.addRecipe(recipe)
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@CreateRecipeFormActivity, "Failed to upload image: ${response.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}