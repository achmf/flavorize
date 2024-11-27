package com.example.flavorize.data.recipedraft

import androidx.room.*

@Dao
interface DraftRecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: DraftRecipe)

    @Query("SELECT * FROM draft_recipes WHERE userId = :userId")
    suspend fun getDraftsByUser(userId: String): List<DraftRecipe> // Hanya ambil draft milik user tertentu

    @Delete
    suspend fun deleteDraft(draft: DraftRecipe)
}
