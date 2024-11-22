package com.example.flavorize.data.recipedraft

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "draft_recipes")
data class DraftRecipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val servings: Int,
    val cookingTime: Int,
    val ingredients: List<String>,
    val instructions: List<String>,
    val imageUri: String? = null // URI gambar lokal
)
