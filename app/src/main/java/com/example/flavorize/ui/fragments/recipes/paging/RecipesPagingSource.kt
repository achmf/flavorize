package com.example.flavorize.ui.fragments.recipes.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.flavorize.data.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class RecipesPagingSource(
    private val firestore: FirebaseFirestore
) : PagingSource<QuerySnapshot, Recipe>() {

    // Load data for the current page
    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Recipe> {
        return try {
            val currentPage = params.key // Current page key
            val pageSize = params.loadSize // Number of items to load

            // Query Firestore to fetch recipes
            val query = firestore.collection("recipes")
                .orderBy("name") // Sort recipes by name
                .limit(pageSize.toLong()) // Limit the number of items per page

            val snapshot = if (currentPage == null) {
                query.get().await() // Fetch the first page
            } else {
                query.startAfter(currentPage.documents.last()).get().await() // Fetch subsequent pages
            }

            // Map Firestore data to Recipe objects and fetch user names
            val recipes = snapshot.toObjects(Recipe::class.java).map { recipe ->
                val userName = getUserNameById(recipe.userId) // Fetch the user's name
                recipe.copy(userName = userName) // Add userName to the Recipe model
            }

            // Check if more data is available for pagination
            val nextKey = if (recipes.isEmpty() || snapshot.size() < pageSize) null else snapshot

            // Return the loaded data as a page
            LoadResult.Page(
                data = recipes,
                prevKey = null,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            // Return an error if data fetching fails
            LoadResult.Error(e)
        }
    }

    // Determine the key for refreshing data
    override fun getRefreshKey(state: PagingState<QuerySnapshot, Recipe>): QuerySnapshot? {
        return state.anchorPosition?.let { position ->
            // Find the closest page to the user's position
            state.closestPageToPosition(position)?.nextKey
        }
    }

    // Fetch the user's name by their ID from Firestore
    private suspend fun getUserNameById(userId: String): String {
        return try {
            val userDocument = firestore.collection("users").document(userId).get().await()
            userDocument.getString("name") ?: "Unknown User" // Default to "Unknown User" if name is missing
        } catch (e: Exception) {
            "Unknown User" // Default to "Unknown User" if an error occurs
        }
    }
}
