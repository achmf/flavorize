package com.example.flavorize.ui.fragments.myrecipes.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.flavorize.data.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class MyRecipesPagingSource(
    private val firestore: FirebaseFirestore,
    private val userId: String
) : PagingSource<QuerySnapshot, Recipe>() {

    // Load recipes data for the current page
    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Recipe> {
        return try {
            val currentPage = params.key
            val pageSize = params.loadSize

            // Query recipes for the logged-in user ordered by name
            val query = firestore.collection("recipes")
                .whereEqualTo("userId", userId)
                .orderBy("name")
                .limit(pageSize.toLong())

            val snapshot = if (currentPage == null) {
                // Fetch the first page if no current page exists
                query.get().await()
            } else {
                // Fetch the next page starting after the last document of the current page
                query.startAfter(currentPage.documents.last()).get().await()
            }

            // Convert the query result to a list of Recipe objects
            val recipes = snapshot.toObjects(Recipe::class.java)

            // Determine the next page key, or set to null if no more pages
            val nextKey = if (recipes.isEmpty() || snapshot.size() < pageSize) null else snapshot

            // Return the current page with recipes data
            LoadResult.Page(
                data = recipes,
                prevKey = null,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            // Return an error result if an exception occurs
            LoadResult.Error(e)
        }
    }

    // Determine the key for refreshing the data
    override fun getRefreshKey(state: PagingState<QuerySnapshot, Recipe>): QuerySnapshot? {
        return state.anchorPosition?.let { position ->
            // Find the closest page to the user's anchor position
            state.closestPageToPosition(position)?.nextKey
        }
    }
}
