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

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Recipe> {
        return try {
            val currentPage = params.key
            val pageSize = params.loadSize

            // Query Firestore
            val query = firestore.collection("recipes")
                .orderBy("name") // Sort by a field; ensure the field is indexed in Firestore
                .limit(pageSize.toLong())

            val snapshot = if (currentPage == null) {
                query.get().await() // First page
            } else {
                query.startAfter(currentPage.documents.last()).get().await() // Subsequent pages
            }

            val recipes = snapshot.toObjects(Recipe::class.java)

            // Detect the end of data
            val nextKey = if (recipes.isEmpty() || snapshot.size() < pageSize) null else snapshot

            LoadResult.Page(
                data = recipes,
                prevKey = null,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Recipe>): QuerySnapshot? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.nextKey
        }
    }
}
