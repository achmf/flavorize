package com.example.flavorize.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Recipe(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val servings: Int = 0,
    val cookingTime: String = "",
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val userId: String = "",
    val imageUrl: String = "",
    val userName: String = "-",
    val bookmarkedBy: List<String> = emptyList(), // Tambahkan ini
    var isBookmarked: Boolean = false // New property
) : Parcelable
