package com.example.flavorize.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

class SupabaseStorageRepository {
    companion object {
        val supabase = createSupabaseClient(
            supabaseUrl = "https://yjzvfbqooezpwkkwauhp.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlqenZmYnFvb2V6cHdra3dhdWhwIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczMTkzNTc3NywiZXhwIjoyMDQ3NTExNzc3fQ.mu_NeyDgPa9h1GK_hYD1o64Onve2KcsIkXmT-nvgRiQ" // Ganti dengan Service Role Key
        ) {
            install(Postgrest)
            install(Storage)
        }
    }

    // Function to upload an image to Supabase Storage
    suspend fun uploadRecipeImage(imageData: ByteArray, imageName: String): Result<String> {
        return try {
            val path = "$imageName.jpg"
            val bucket = supabase.storage.from("Recipe Images")
            val response = bucket.upload(path, imageData) {
                upsert = true // Mengizinkan overwrite jika nama file sudah ada
            }
            val imageUrl = bucket.publicUrl(path)
            Result.success(imageUrl)
        } catch (e: Exception) {
            // Tambahkan logging untuk mengetahui error
            e.printStackTrace()
            Result.failure(e)
        }
    }
}