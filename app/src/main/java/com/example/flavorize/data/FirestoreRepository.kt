package com.example.flavorize.data

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.flavorize.ui.fragments.myrecipes.paging.MyRecipesPagingSource
import com.example.flavorize.ui.fragments.recipes.paging.RecipesPagingSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val recipesCollection = firestore.collection("recipes")
    private var recipeChangeListener: ListenerRegistration? = null
    private val usersCollection = firestore.collection("users")
    private val commentsCollection = firestore.collection("comments") // Koleksi "comments"
    private val auth = FirebaseAuth.getInstance()
    private val bookmarkListeners = mutableMapOf<String, ListenerRegistration>()

    // User Authentication
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    fun getCurrentUserName(): String = auth.currentUser?.displayName ?: "Anonymous"

    // Recipe Management
    suspend fun addRecipe(recipe: Recipe): Result<Void?> {
        return try {
            val userId = auth.currentUser?.uid ?: throw FirebaseFirestoreException(
                "User not authenticated",
                FirebaseFirestoreException.Code.PERMISSION_DENIED
            )
            val recipeWithUserId = recipe.copy(id = UUID.randomUUID().toString(), userId = userId)
            recipesCollection.document(recipeWithUserId.id).set(recipeWithUserId).await()
            Result.success(null)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e)
        }
    }

    suspend fun deleteRecipe(recipeId: String): Result<Void?> {
        return try {
            recipesCollection.document(recipeId).delete().await()
            Result.success(null)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e)
        }
    }

    suspend fun updateRecipe(recipeId: String, updatedRecipe: Recipe): Result<Void?> {
        return try {
            recipesCollection.document(recipeId).set(updatedRecipe).await()
            Result.success(null)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e)
        }
    }

    fun getPagedRecipes(): Flow<PagingData<Recipe>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { RecipesPagingSource(firestore) }
        ).flow.map { pagingData ->
            pagingData.map { recipe ->
                val userName = getUserNameById(recipe.userId)
                recipe.copy(userName = userName)
            }
        }
    }

    fun getPagedMyRecipes(userId: String): Flow<PagingData<Recipe>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { MyRecipesPagingSource(firestore, userId) }
        ).flow.map { pagingData ->
            pagingData.map { recipe ->
                val userName = getUserNameById(recipe.userId)
                recipe.copy(userName = userName)
            }
        }
    }

    fun listenForRecipeChanges(userId: String, onUpdate: (List<Recipe>) -> Unit) {
        val query = firestore.collection("recipes").whereEqualTo("userId", userId)
        recipeChangeListener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            snapshot?.let {
                val recipes = it.toObjects(Recipe::class.java)
                onUpdate(recipes)
            }
        }
    }

    fun stopListeningForRecipeChanges() {
        recipeChangeListener?.remove()
        recipeChangeListener = null
    }

    // User Management
    suspend fun getUserNameById(userId: String): String {
        return try {
            val userDocument = usersCollection.document(userId).get().await()
            userDocument.getString("name") ?: "Unknown User"
        } catch (e: FirebaseFirestoreException) {
            "Unknown User"
        }
    }

    // Bookmark Management
    suspend fun addBookmark(userId: String, recipeId: String): Result<Void?> {
        return try {
            firestore.collection("users").document(userId).collection("bookmarks")
                .document(recipeId)
                .set(mapOf("recipeId" to recipeId)).await()
            recipesCollection.document(recipeId)
                .update("bookmarkedBy", FieldValue.arrayUnion(userId)).await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeBookmark(userId: String, recipeId: String): Result<Void?> {
        return try {
            firestore.collection("users").document(userId).collection("bookmarks")
                .document(recipeId)
                .delete().await()
            recipesCollection.document(recipeId)
                .update("bookmarkedBy", FieldValue.arrayRemove(userId)).await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserBookmarkedRecipes(userId: String): Result<List<Recipe>> {
        return try {
            val bookmarksSnapshot = firestore.collection("users")
                .document(userId)
                .collection("bookmarks")
                .get()
                .await()

            val bookmarkedRecipes = bookmarksSnapshot.documents.mapNotNull { doc ->
                val recipeId = doc.id
                recipesCollection.document(recipeId).get().await().toObject(Recipe::class.java)
            }

            Result.success(bookmarkedRecipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenForBookmarkChanges(userId: String, onBookmarksChanged: (Set<String>) -> Unit) {
        val bookmarksRef = firestore.collection("users").document(userId).collection("bookmarks")
        val listener = bookmarksRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val bookmarkIds = snapshot.documents.mapNotNull { it.id }.toSet()
            onBookmarksChanged(bookmarkIds)
        }
        bookmarkListeners[userId] = listener
    }

    fun stopListeningForBookmarkChanges() {
        bookmarkListeners.forEach { (_, registration) ->
            registration.remove()
        }
        bookmarkListeners.clear()
    }

    // Comments Management
    suspend fun getCommentsForRecipe(recipeId: String): Result<List<RecipeComment>> {
        return try {
            val snapshot = commentsCollection.whereEqualTo("recipeId", recipeId).get().await()
            val comments = snapshot.toObjects(RecipeComment::class.java)
            Result.success(comments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addCommentToRecipe(recipeId: String, comment: RecipeComment): Result<Void?> {
        return try {
            val commentWithRecipeId = comment.copy(recipeId = recipeId)
            commentsCollection.document().set(commentWithRecipeId).await()
            Result.success(null)
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error adding comment: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateComment(comment: RecipeComment): Result<Void?> {
        return try {
            val doc = commentsCollection.whereEqualTo("recipeId", comment.recipeId)
                .whereEqualTo("userId", comment.userId)
                .limit(1)
                .get()
                .await()
                .documents.firstOrNull()

            doc?.reference?.update("text", comment.text)?.await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteComment(comment: RecipeComment): Result<Void?> {
        return try {
            val doc = commentsCollection.whereEqualTo("recipeId", comment.recipeId)
                .whereEqualTo("userId", comment.userId)
                .limit(1)
                .get()
                .await()
                .documents.firstOrNull()

            doc?.reference?.delete()?.await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
