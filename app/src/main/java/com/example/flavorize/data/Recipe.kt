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
    val userId: String = "", // Add userId to track the user who created the recipe
    val imageUrl: String = "" // Add imageUrl to store the uploaded image of the recipe
)