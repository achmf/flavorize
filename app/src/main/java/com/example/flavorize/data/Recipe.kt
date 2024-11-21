package com.example.flavorize.data

import java.util.UUID

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
    val userName: String = "Unknown User" // Add userName to track the user's name who created the recipe
)
