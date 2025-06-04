package com.example.flavorize.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

class SupabaseStorageRepository {
    companion object {
        // Initialize Supabase client with updated credentials
        val supabase = createSupabaseClient(
            supabaseUrl = "https://cfondjisoqhyfdlkzigd.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNmb25kamlzb3FoeWZkbGt6aWdkIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0OTAxNDY5MSwiZXhwIjoyMDY0NTkwNjkxfQ.kCnOj4_IMVjiLojIY-wJ-0Erwruv9KctkloAuybPPDU"
        ) {
            install(Postgrest) // Enable Postgrest for database queries
            install(Storage)   // Enable Storage for file uploads
        }
    }

    /**
     * Upload recipe image to Supabase Storage
     *
     * @param imageData ByteArray of the image
     * @param imageName Name of the file
     * @return Result with public URL or error
     */
    suspend fun uploadRecipeImage(imageData: ByteArray, imageName: String): Result<String> {
        return try {
            val path = "$imageName.jpg" // File path in storage
            val bucket = supabase.storage.from("recipe-images") // Get the bucket
            bucket.upload(path, imageData) { upsert = true } // Upload image, allow overwrite
            val imageUrl = bucket.publicUrl(path) // Get public URL
            Result.success(imageUrl)
        } catch (e: Exception) {
            e.printStackTrace() // Print error for debugging
            Result.failure(e) // Return failure
        }
    }
}