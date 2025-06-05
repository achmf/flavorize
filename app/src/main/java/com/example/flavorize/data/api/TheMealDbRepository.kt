package com.example.flavorize.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.HttpURLConnection
import java.net.URL

/**
 * Repository class for TheMealDB API
 */class TheMealDbRepository {
    private val baseUrl = "https://www.themealdb.com/api/json/v1/1/"

    // Initialize Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(TheMealDbApi::class.java)

    /**
     * Fetch random meals from TheMealDB API
     *
     * @return Result containing a list of MealDbRecipe or an exception
     */
    suspend fun getRandomMeals(count: Int = 10): Result<List<MealDbRecipe>> {
        return withContext(Dispatchers.IO) {
            try {
                val meals = mutableListOf<MealDbRecipe>()

                // We need to make multiple calls to get multiple random meals
                repeat(count) {
                    val connection = URL("${baseUrl}random.php").openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        val jsonObject = JSONObject(response)
                        val mealsArray = jsonObject.getJSONArray("meals")

                        if (mealsArray.length() > 0) {
                            val mealObject = mealsArray.getJSONObject(0)
                            val meal = MealDbRecipe(
                                id = mealObject.getString("idMeal"),
                                name = mealObject.getString("strMeal"),
                                category = mealObject.optString("strCategory", ""),
                                area = mealObject.optString("strArea", ""),
                                instructions = mealObject.optString("strInstructions", ""),
                                imageUrl = mealObject.optString("strMealThumb", ""),
                                youtubeUrl = mealObject.optString("strYoutube", ""),
                                ingredients = extractIngredients(mealObject)
                            )
                            meals.add(meal)
                        }
                    } else {
                        return@withContext Result.failure(Exception("Failed to fetch meal: HTTP $responseCode"))
                    }
                    connection.disconnect()
                }
                Result.success(meals)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    /**
     * Fetch meals by category from TheMealDB API
     *
     * @param category The category name to filter by
     * @return Result containing a list of MealDbRecipe or an exception
     */
    suspend fun getMealsByCategory(category: String): Result<List<MealDbRecipe>> {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL("${baseUrl}filter.php?c=$category").openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(response)
                    val mealsArray = jsonObject.getJSONArray("meals")

                    val meals = mutableListOf<MealDbRecipe>()
                    for (i in 0 until mealsArray.length()) {
                        val mealObject = mealsArray.getJSONObject(i)
                        val mealId = mealObject.getString("idMeal")

                        // Fetch full details for each meal
                        val mealDetails = getMealById(mealId)
                        if (mealDetails.isSuccess) {
                            mealDetails.getOrNull()?.let { meals.add(it) }
                        }
                    }
                    connection.disconnect()
                    Result.success(meals)
                } else {
                    connection.disconnect()
                    Result.failure(Exception("Failed to fetch meals: HTTP $responseCode"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    /**
     * Fetch meal details by ID from TheMealDB API
     *
     * @param id The meal ID
     * @return Result containing a MealDbRecipe or an exception
     */
    suspend fun getMealById(id: String): Result<MealDbRecipe> {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL("${baseUrl}lookup.php?i=$id").openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(response)
                    val mealsArray = jsonObject.getJSONArray("meals")

                    if (mealsArray.length() > 0) {
                        val mealObject = mealsArray.getJSONObject(0)
                        val meal = MealDbRecipe(
                            id = mealObject.getString("idMeal"),
                            name = mealObject.getString("strMeal"),
                            category = mealObject.optString("strCategory", ""),
                            area = mealObject.optString("strArea", ""),
                            instructions = mealObject.optString("strInstructions", ""),
                            imageUrl = mealObject.optString("strMealThumb", ""),
                            youtubeUrl = mealObject.optString("strYoutube", ""),
                            ingredients = extractIngredients(mealObject)
                        )
                        connection.disconnect()
                        Result.success(meal)
                    } else {
                        connection.disconnect()
                        Result.failure(Exception("Meal not found"))
                    }
                } else {
                    connection.disconnect()
                    Result.failure(Exception("Failed to fetch meal: HTTP $responseCode"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    /**
     * Helper method to extract ingredients from JSONObject
     */
    private fun extractIngredients(jsonObject: JSONObject): List<String> {
        val ingredients = mutableListOf<String>()

        for (i in 1..20) {
            val ingredient = jsonObject.optString("strIngredient$i", "")
            val measure = jsonObject.optString("strMeasure$i", "")

            if (ingredient.isNotBlank() && ingredient != "null") {
                val ingredientText = if (measure.isNotBlank() && measure != "null") {
                    "$measure $ingredient"
                } else {
                    ingredient
                }
                ingredients.add(ingredientText)
            }
        }

        return ingredients
    }

    /**
     * TheMealDB API interface for Retrofit
     */
    interface TheMealDbApi {
        /**
         * Get a random meal
         */
        @GET("random.php")
        suspend fun getRandomMeal(): MealDbApiResponse

        /**
         * Get all categories
         */
        @GET("categories.php")
        suspend fun getCategories(): CategoryResponse

        /**
         * Get meals by category
         */
        @GET("filter.php")
        suspend fun getMealsByCategory(@Query("c") category: String): MealDbApiResponse

        /**
         * Get meal details by ID
         */
        @GET("lookup.php")
        suspend fun getMealById(@Query("i") id: String): MealDbApiResponse
    }
}

data class MealDbRecipe(
    val id: String,
    val name: String,
    val category: String,
    val area: String,
    val instructions: String,
    val imageUrl: String,
    val youtubeUrl: String,
    val ingredients: List<String>
)

data class MealDbApiResponse(
    val meals: List<MealDbRecipe>
)

data class CategoryResponse(
    val categories: List<Category>
)

data class Category(
    val idCategory: String,
    val strCategory: String,
    val strCategoryThumb: String,
    val strCategoryDescription: String
)
