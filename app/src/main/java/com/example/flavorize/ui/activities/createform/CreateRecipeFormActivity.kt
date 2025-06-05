package com.example.flavorize.ui.activities.createform

import android.content.Intent
import android.graphics.drawable.Drawable
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
import androidx.lifecycle.lifecycleScope
import com.example.flavorize.R
import com.example.flavorize.data.Recipe
import com.example.flavorize.data.SupabaseStorageRepository
import com.example.flavorize.data.recipedraft.DraftRecipe
import com.example.flavorize.data.recipedraft.DraftRecipeDatabase
import com.example.flavorize.databinding.ActivityCreateRecipeFormBinding
import com.example.flavorize.ui.activities.createform.viewmodel.CreateRecipeViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
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

        setupToolbar() // Setup toolbar
        setupUI() // Setup UI components
        observeViewModel() // Observe ViewModel LiveData

        // Populate the form with a draft recipe if provided
        val draft = intent.getParcelableExtra<DraftRecipe>("draft")
        draft?.let { populateFormWithDraft(it) }
    }

    private fun setupToolbar() {
        // Configure the toolbar
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
        // Handle form submission
        binding.submitRecipeButton.setOnClickListener {
            if (!isSubmitClicked) {
                isSubmitClicked = true
                handlePostRecipe()
            }
        }

        // Handle saving as draft
        binding.draftRecipeButton.setOnClickListener {
            if (!isDraftClicked) {
                isDraftClicked = true
                handleSaveDraft()
            }
        }

        // Add dynamic ingredient fields above the button
        val addIngredientButton = binding.addIngredientButton
        val recipeIngredientsLayout = binding.recipeIngredientsLayout

        addIngredientButton.setOnClickListener {
            addIngredientField(recipeIngredientsLayout, addIngredientButton, "")
        }

        // Add dynamic instruction fields above the button
        val addInstructionButton = binding.addInstructionButton
        val recipeInstructionsLayout = binding.recipeInstructionsLayout

        addInstructionButton.setOnClickListener {
            addInstructionField(recipeInstructionsLayout, addInstructionButton, "")
        }

        // Handle recipe image selection
        val recipeImageView = binding.recipeImageView
        recipeImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
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
            hintTextColor = resources.getColorStateList(android.R.color.holo_orange_light, theme)
        }

        val ingredientEditText = TextInputEditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setText(initialText)
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
            setImageResource(R.drawable.ic_close)
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
            hintTextColor = resources.getColorStateList(android.R.color.holo_orange_light, theme)
        }

        val instructionEditText = TextInputEditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setText(initialText)
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
            setImageResource(R.drawable.ic_close)
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

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // Handle image selection result
        if (result.resultCode == RESULT_OK) {
            imageUri = result.data?.data
            binding.recipeImageView.setImageURI(imageUri)
        }
    }

    private fun observeViewModel() {
        // Observe the result of adding a recipe
        viewModel.addRecipeResult.observe(this) { result ->
            if (result.isSuccess) {
                Toast.makeText(this, "Recipe added successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                isSubmitClicked = false // Allow retry on failure
                Toast.makeText(this, "Failed to add recipe: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handlePostRecipe() {
        // Handle recipe posting logic
        val recipe = getCurrentRecipe()
        if (recipe != null) {
            uploadImageAndSaveRecipe(recipe)
        } else {
            isSubmitClicked = false // Reset if validation fails
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSaveDraft() {
        // Handle saving the recipe as a draft
        val originalRecipe = getCurrentDraftRecipe()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (originalRecipe != null && userId != null) {
            val cachedImageUri = imageUri?.let { uri -> saveImageToCache(uri) }
            val updatedRecipe = originalRecipe.copy(userId = userId, imageUri = cachedImageUri)

            lifecycleScope.launch {
                draftDao.insertDraft(updatedRecipe)
                Toast.makeText(this@CreateRecipeFormActivity, "Recipe saved as draft", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            isDraftClicked = false // Reset if validation fails
            Toast.makeText(this, "Please complete name and description fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageToCache(uri: Uri): String? {
        // Save selected image to the cache directory
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(cacheDir, "${UUID.randomUUID()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun uploadImageAndSaveRecipe(recipe: Recipe) {
        // Upload the image and save the recipe
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
        // Get the current recipe data from the form
        val name = binding.recipeNameEditText.text.toString().trim()
        val description = binding.recipeDescriptionEditText.text.toString().trim()
        val servings = binding.recipePortionsEditText.text.toString().toIntOrNull() ?: 0
        val cookingTime = binding.recipeCookingTimeEditText.text.toString().toIntOrNull() ?: 0

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
        // Get the current draft recipe data from the form
        val name = binding.recipeNameEditText.text.toString().trim()
        val description = binding.recipeDescriptionEditText.text.toString().trim()
        val servings = binding.recipePortionsEditText.text.toString().toIntOrNull() ?: 0
        val cookingTime = binding.recipeCookingTimeEditText.text.toString().toIntOrNull() ?: 0
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return null

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
            DraftRecipe(
                userId = userId,
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

    private fun populateFormWithDraft(draft: DraftRecipe) {
        // Populate the form fields with the draft data
        binding.recipeNameEditText.setText(draft.name)
        binding.recipeDescriptionEditText.setText(draft.description)
        binding.recipePortionsEditText.setText(draft.servings.toString())
        binding.recipeCookingTimeEditText.setText(draft.cookingTime.toString())

        // Populate ingredients
        val addIngredientButton = binding.addIngredientButton
        val recipeIngredientsLayout = binding.recipeIngredientsLayout

        draft.ingredients.forEach { ingredient ->
            addIngredientField(recipeIngredientsLayout, addIngredientButton, ingredient)
        }

        // Populate instructions
        val addInstructionButton = binding.addInstructionButton
        val recipeInstructionsLayout = binding.recipeInstructionsLayout

        draft.instructions.forEach { instruction ->
            addInstructionField(recipeInstructionsLayout, addInstructionButton, instruction)
        }

        // Load the image if available
        draft.imageUri?.let { uri ->
            val cachedUri = saveImageToCache(uri.toUri())
            cachedUri?.let {
                val drawable = Drawable.createFromPath(it)
                binding.recipeImageView.setImageDrawable(drawable)
                imageUri = it.toUri()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        uploadJob?.cancel() // Cancel any ongoing upload job
    }
}
