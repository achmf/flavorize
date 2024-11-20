package com.example.flavorize.data

data class Recipe(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val servings: Int = 0,
    val cookingTime: String = "",
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList()
)
