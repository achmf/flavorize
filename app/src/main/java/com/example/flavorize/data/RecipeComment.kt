package com.example.flavorize.data

data class RecipeComment(
    val recipeId: String = "",
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)
