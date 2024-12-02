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

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Recipe> {
        return try {
            val currentPage = params.key
            val pageSize = params.loadSize

            // Query hanya untuk resep dengan userId sesuai user yang sedang login
            val query = firestore.collection("recipes")
                .whereEqualTo("userId", userId)
                .orderBy("name")
                .limit(pageSize.toLong())

            val snapshot = if (currentPage == null) {
                query.get().await()
            } else {
                query.startAfter(currentPage.documents.last()).get().await()
            }

            val recipes = snapshot.toObjects(Recipe::class.java)
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
