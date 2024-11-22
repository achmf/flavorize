package com.example.flavorize.data.recipedraft

import androidx.room.*

@Dao
interface DraftRecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: DraftRecipe)

    @Query("SELECT * FROM draft_recipes")
    suspend fun getAllDrafts(): List<DraftRecipe>

    @Delete
    suspend fun deleteDraft(draft: DraftRecipe)
}
