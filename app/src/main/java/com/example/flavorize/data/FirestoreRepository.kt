package com.example.flavorize.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val recipesCollection = firestore.collection("recipes")
    private val usersCollection = firestore.collection("users")
    private val auth = FirebaseAuth.getInstance()

    // Function to add a new recipe to Firestore
    suspend fun addRecipe(recipe: Recipe): Result<Void?> {
        return try {
            val userId = auth.currentUser?.uid ?: throw FirebaseFirestoreException("User not authenticated", FirebaseFirestoreException.Code.PERMISSION_DENIED)
            val recipeWithUserId = recipe.copy(id = UUID.randomUUID().toString(), userId = userId)
            recipesCollection.document(recipeWithUserId.id).set(recipeWithUserId).await()
            Result.success(null)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e)
        }
    }

    // Function to get all recipes from Firestore
    suspend fun getAllRecipes(): Result<List<Recipe>> {
        return try {
            val snapshot = recipesCollection.get().await()
            val recipes = snapshot.toObjects(Recipe::class.java)

            // Fetch user names for each recipe
            val recipesWithUserNames = recipes.map { recipe ->
                val userName = getUserNameById(recipe.userId)
                recipe.copy(userName = userName)
            }
            Result.success(recipesWithUserNames)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e)
        }
    }

    suspend fun getRecipesByCurrentUser(): Result<List<Recipe>> {
        return try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
                ?: throw Exception("User not authenticated")
            val snapshot = recipesCollection.whereEqualTo("userId", userId).get().await()
            val recipes = snapshot.toObjects(Recipe::class.java)

            // Fetch user names for each recipe
            val recipesWithUserNames = recipes.map { recipe ->
                val userName = getUserNameById(recipe.userId)
                recipe.copy(userName = userName)
            }

            Result.success(recipesWithUserNames)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    // Function to get a single recipe by ID
    suspend fun getRecipeById(recipeId: String): Result<Recipe> {
        return try {
            val document = recipesCollection.document(recipeId).get().await()
            val recipe = document.toObject(Recipe::class.java)
            if (recipe != null) {
                val userName = getUserNameById(recipe.userId)
                Result.success(recipe.copy(userName = userName))
            } else {
                Result.failure(Exception("Recipe not found"))
            }
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e)
        }
    }

    // Function to update a recipe
    suspend fun updateRecipe(recipeId: String, updatedRecipe: Recipe): Result<Void?> {
        return try {
            recipesCollection.document(recipeId).set(updatedRecipe).await()
            Result.success(null)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e)
        }
    }

    // Function to get a user's name by ID
    private suspend fun getUserNameById(userId: String): String {
        return try {
            val userDocument = usersCollection.document(userId).get().await()
            userDocument.getString("name") ?: "Unknown User"
        } catch (e: FirebaseFirestoreException) {
            "Unknown User"
        }
    }
}