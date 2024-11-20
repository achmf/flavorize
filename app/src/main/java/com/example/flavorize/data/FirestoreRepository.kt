// FirestoreRepository.kt
package com.example.flavorize.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val recipesCollection = firestore.collection("recipes")
    private val auth = FirebaseAuth.getInstance()

    companion object {
        val supabase = createSupabaseClient(
            supabaseUrl = "https://yjzvfbqooezpwkkwauhp.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlqenZmYnFvb2V6cHdra3dhdWhwIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczMTkzNTc3NywiZXhwIjoyMDQ3NTExNzc3fQ.mu_NeyDgPa9h1GK_hYD1o64Onve2KcsIkXmT-nvgRiQ" // Ganti dengan Service Role Key
        ) {
            install(Postgrest)
            install(Storage)
        }
    }

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

    // Function to upload an image to Supabase Storage
    suspend fun uploadRecipeImage(imageData: ByteArray, imageName: String): Result<String> {
        return try {
            val path = "$imageName.jpg"
            val bucket = supabase.storage.from("Recipe Images")
            val response = bucket.upload(path, imageData) {
                upsert = false
            }
            val imageUrl = bucket.publicUrl(path)
            Result.success(imageUrl)
        } catch (e: StorageException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
