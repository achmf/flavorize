package com.example.flavorize.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val bookmarksCollection = firestore.collection("bookmarks")
    private val recipesCollection = firestore.collection("recipes")
    private val usersCollection = firestore.collection("users")
    val auth = FirebaseAuth.getInstance()

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

    // Function to delete a recipe by ID
    suspend fun deleteRecipe(recipeId: String): Result<Void?> {
        return try {
            recipesCollection.document(recipeId).delete().await()
            Result.success(null)
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
    suspend fun getUserNameById(userId: String): String {
        return try {
            val userDocument = usersCollection.document(userId).get().await()
            userDocument.getString("name") ?: "Unknown User"
        } catch (e: FirebaseFirestoreException) {
            "Unknown User"
        }
    }

    suspend fun addBookmark(userId: String, recipeId: String): Result<Void?> {
        return try {
            bookmarksCollection.document(userId).update("bookmarkedRecipes", FieldValue.arrayUnion(recipeId)).await()
            Result.success(null)
        } catch (e: FirebaseFirestoreException) {
            // If document doesn't exist, create it
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                bookmarksCollection.document(userId).set(mapOf("bookmarkedRecipes" to listOf(recipeId))).await()
                Result.success(null)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getUserBookmarkedRecipes(userId: String): Result<List<Recipe>> {
        return try {
            val document = bookmarksCollection.document(userId).get().await()
            val bookmarkedRecipeIds = document.get("bookmarkedRecipes") as? List<String> ?: emptyList()

            if (bookmarkedRecipeIds.isEmpty()) {
                return Result.success(emptyList()) // Return an empty list if no bookmarks
            }

            // Fetch recipes using the bookmarked IDs
            val recipes = recipesCollection.whereIn("id", bookmarkedRecipeIds).get().await().toObjects(Recipe::class.java)

            // Attach user names to the recipes
            val recipesWithUserNames = recipes.map { recipe ->
                val userName = getUserNameById(recipe.userId)
                recipe.copy(userName = userName)
            }

            Result.success(recipesWithUserNames)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeBookmark(userId: String, recipeId: String): Result<Void?> {
        return try {
            bookmarksCollection.document(userId).update("bookmarkedRecipes", FieldValue.arrayRemove(recipeId)).await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Function to get all bookmarked recipes for a user
    suspend fun getBookmarkedRecipes(userId: String): Result<List<Recipe>> {
        return try {
            val document = bookmarksCollection.document(userId).get().await()
            val bookmarkedRecipeIds = document.get("bookmarkedRecipes") as? List<String> ?: emptyList()
            val recipes = recipesCollection.whereIn("id", bookmarkedRecipeIds).get().await().toObjects(Recipe::class.java)
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Function to get all recipes created by a specific user
    suspend fun getAllRecipesForUser(userId: String): Result<List<Recipe>> {
        return try {
            val snapshot = recipesCollection.whereEqualTo("userId", userId).get().await()
            val recipes = snapshot.toObjects(Recipe::class.java)
            Result.success(recipes)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e)
        }
    }

    // New function to check bookmarked status
    suspend fun fetchRecipesWithBookmarkStatus(userId: String): Result<List<Recipe>> {
        return try {
            val snapshot = recipesCollection.get().await()
            val allRecipes = snapshot.toObjects(Recipe::class.java)

            // Get bookmarked recipe IDs for the user
            val userBookmarksDoc = bookmarksCollection.document(userId).get().await()
            val bookmarkedRecipeIds = userBookmarksDoc.get("bookmarkedRecipes") as? List<String> ?: emptyList()

            // Update recipes with bookmark status and fetch user names
            val recipesWithBookmarkStatus = allRecipes.map { recipe ->
                val userName = getUserNameById(recipe.userId)
                recipe.copy(
                    isBookmarked = bookmarkedRecipeIds.contains(recipe.id),
                    userName = userName
                )
            }

            Result.success(recipesWithBookmarkStatus)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
