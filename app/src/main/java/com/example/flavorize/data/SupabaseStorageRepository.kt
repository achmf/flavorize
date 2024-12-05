package com.example.flavorize.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

class SupabaseStorageRepository {
    companion object {
        // Initialize Supabase client
        val supabase = createSupabaseClient(
            supabaseUrl = "https://yjzvfbqooezpwkkwauhp.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlqenZmYnFvb2V6cHdra3dhdWhwIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczMTkzNTc3NywiZXhwIjoyMDQ3NTExNzc3fQ.mu_NeyDgPa9h1GK_hYD1o64Onve2KcsIkXmT-nvgRiQ" // Replace with your Service Role Key
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
            val bucket = supabase.storage.from("Recipe Images") // Get the bucket
            bucket.upload(path, imageData) { upsert = true } // Upload image, allow overwrite
            val imageUrl = bucket.publicUrl(path) // Get public URL
            Result.success(imageUrl)
        } catch (e: Exception) {
            e.printStackTrace() // Print error for debugging
            Result.failure(e) // Return failure
        }
    }
}
