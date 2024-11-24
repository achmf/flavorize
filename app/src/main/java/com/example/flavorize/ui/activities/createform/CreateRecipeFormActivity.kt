package com.example.flavorize.ui.activities.createform

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
import androidx.lifecycle.lifecycleScope
import com.example.flavorize.data.recipedraft.DraftRecipe
import com.example.flavorize.data.recipedraft.DraftRecipeDatabase
import com.example.flavorize.data.Recipe
import com.example.flavorize.data.SupabaseStorageRepository
import com.example.flavorize.databinding.ActivityCreateRecipeFormBinding
import com.example.flavorize.ui.activities.createform.viewmodel.CreateRecipeViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.UUID

class CreateRecipeFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateRecipeFormBinding
    private val viewModel: CreateRecipeViewModel by viewModels()
    private var imageUri: Uri? = null
    private var uploadJob: Job? = null
    private val draftDao by lazy { DraftRecipeDatabase.getDatabase(this).draftRecipeDao() }
    private var isSubmitClicked = false
    private var isDraftClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateRecipeFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupUI()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Create Recipe"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupUI() {
        binding.submitRecipeButton.setOnClickListener {
            if (!isSubmitClicked) {
                isSubmitClicked = true
                handlePostRecipe()
            }
        }

        binding.draftRecipeButton.setOnClickListener {
            if (!isDraftClicked) {
                isDraftClicked = true
                handleSaveDraft()
            }
        }

        val addIngredientButton = binding.addIngredientButton
        val recipeIngredientsLayout = binding.recipeIngredientsLayout

        addIngredientButton.setOnClickListener {
            val ingredientEditText = EditText(this)
            ingredientEditText.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            ingredientEditText.hint = "Ingredient"
            recipeIngredientsLayout.addView(ingredientEditText)
        }

        val addInstructionButton = binding.addInstructionButton
        val recipeInstructionsLayout = binding.recipeInstructionsLayout

        addInstructionButton.setOnClickListener {
            val instructionEditText = EditText(this)
            instructionEditText.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            instructionEditText.hint = "Instruction"
            recipeInstructionsLayout.addView(instructionEditText)
        }

        val recipeImageView = binding.recipeImageView
        recipeImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            binding.recipeImageView.setImageURI(imageUri)
        }
    }

    private fun observeViewModel() {
        viewModel.addRecipeResult.observe(this) { result ->
            if (result.isSuccess) {
                Toast.makeText(this, "Recipe added successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                isSubmitClicked = false // Allow user to retry if there is a failure
                Toast.makeText(this, "Failed to add recipe: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handlePostRecipe() {
        val recipe = getCurrentRecipe()
        if (recipe != null) {
            uploadImageAndSaveRecipe(recipe)
        } else {
            isSubmitClicked = false // Reset state if validation fails
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSaveDraft() {
        val recipe = getCurrentDraftRecipe()
        if (recipe != null) {
            lifecycleScope.launch {
                draftDao.insertDraft(recipe)
                Toast.makeText(this@CreateRecipeFormActivity, "com.example.flavorize.data.Recipe saved as draft", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            isDraftClicked = false // Reset state if validation fails
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageAndSaveRecipe(recipe: Recipe) {
        imageUri?.let { uri ->
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            inputStream?.let {
                val imageData = it.readBytes()
                val imageName = UUID.randomUUID().toString()
                uploadJob?.cancel()
                uploadJob = lifecycleScope.launch {
                    val response = SupabaseStorageRepository().uploadRecipeImage(imageData, imageName)
                    if (response.isSuccess) {
                        val imageUrl = response.getOrNull()
                        if (imageUrl != null) {
                            val recipeToPost = recipe.copy(imageUrl = imageUrl)
                            viewModel.addRecipe(recipeToPost)
                        } else {
                            isSubmitClicked = false
                            Toast.makeText(this@CreateRecipeFormActivity, "Image URL not returned", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        isSubmitClicked = false
                        Toast.makeText(
                            this@CreateRecipeFormActivity,
                            "Failed to upload image: ${response.exceptionOrNull()?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } ?: run {
                isSubmitClicked = false
                Toast.makeText(this, "Failed to read image data", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            isSubmitClicked = false
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentRecipe(): Recipe? {
        val name = binding.recipeNameEditText.text.toString()
        val description = binding.recipeDescriptionEditText.text.toString()
        val servings = binding.recipePortionsEditText.text.toString().toIntOrNull() ?: 0
        val cookingTime = binding.recipeCookingTimeEditText.text.toString().toIntOrNull() ?: 0

        val ingredients = mutableListOf<String>()
        for (i in 0 until binding.recipeIngredientsLayout.childCount) {
            val view = binding.recipeIngredientsLayout.getChildAt(i)
            if (view is EditText) {
                val ingredient = view.text.toString()
                if (ingredient.isNotBlank()) {
                    ingredients.add(ingredient)
                }
            }
        }

        val instructions = mutableListOf<String>()
        for (i in 0 until binding.recipeInstructionsLayout.childCount) {
            val view = binding.recipeInstructionsLayout.getChildAt(i)
            if (view is EditText) {
                val instruction = view.text.toString()
                if (instruction.isNotBlank()) {
                    instructions.add(instruction)
                }
            }
        }

        return if (name.isNotBlank() && description.isNotBlank()) {
            Recipe(
                name = name,
                description = description,
                servings = servings,
                cookingTime = cookingTime.toString(),
                ingredients = ingredients,
                instructions = instructions,
                imageUrl = ""
            )
        } else null
    }

    private fun getCurrentDraftRecipe(): DraftRecipe? {
        val name = binding.recipeNameEditText.text.toString()
        val description = binding.recipeDescriptionEditText.text.toString()
        val servings = binding.recipePortionsEditText.text.toString().toIntOrNull() ?: 0
        val cookingTime = binding.recipeCookingTimeEditText.text.toString().toIntOrNull() ?: 0

        val ingredients = mutableListOf<String>()
        for (i in 0 until binding.recipeIngredientsLayout.childCount) {
            val view = binding.recipeIngredientsLayout.getChildAt(i)
            if (view is EditText) {
                val ingredient = view.text.toString()
                if (ingredient.isNotBlank()) {
                    ingredients.add(ingredient)
                }
            }
        }

        val instructions = mutableListOf<String>()
        for (i in 0 until binding.recipeInstructionsLayout.childCount) {
            val view = binding.recipeInstructionsLayout.getChildAt(i)
            if (view is EditText) {
                val instruction = view.text.toString()
                if (instruction.isNotBlank()) {
                    instructions.add(instruction)
                }
            }
        }

        return if (name.isNotBlank() && description.isNotBlank()) {
            DraftRecipe(
                name = name,
                description = description,
                servings = servings,
                cookingTime = cookingTime,
                ingredients = ingredients,
                instructions = instructions,
                imageUri = imageUri?.toString()
            )
        } else null
    }

    override fun onDestroy() {
        super.onDestroy()
        uploadJob?.cancel()
    }
}
