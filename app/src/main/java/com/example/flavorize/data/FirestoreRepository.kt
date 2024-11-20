package com.example.flavorize.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val recipesCollection = firestore.collection("recipes")

    // Function to add a new recipe to Firestore
    suspend fun addRecipe(recipe: Recipe): Result<Void?> {
        return try {
            recipesCollection.add(recipe).await()
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
            Result.success(recipes)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e)
        }
    }

    // Function to get a single recipe by ID
    suspend fun getRecipeById(recipeId: String): Result<Recipe> {
        return try {
            val document = recipesCollection.document(recipeId).get().await()
            val recipe = document.toObject(Recipe::class.java)
            if (recipe != null) {
                Result.success(recipe)
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

    // Function to delete a recipe
    suspend fun deleteRecipe(recipeId: String): Result<Void?> {
        return try {
            recipesCollection.document(recipeId).delete().await()
            Result.success(null)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e)
        }
    }
}