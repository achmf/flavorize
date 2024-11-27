package com.example.flavorize.data.recipedraft

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "draft_recipes")
data class DraftRecipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val name: String,
    val description: String,
    val servings: Int,
    val cookingTime: Int,
    val ingredients: List<String>,
    val instructions: List<String>,
    val imageUri: String? = null // URI gambar lokal
) : Parcelable
