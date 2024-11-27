package com.example.flavorize.data.recipedraft

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [DraftRecipe::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DraftRecipeDatabase : RoomDatabase() {
    abstract fun draftRecipeDao(): DraftRecipeDao

    companion object {
        @Volatile
        private var INSTANCE: DraftRecipeDatabase? = null

        fun getDatabase(context: Context): DraftRecipeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DraftRecipeDatabase::class.java,
                    "draft_recipe_database"
                )
                    .fallbackToDestructiveMigration() // Hapus data lama jika tidak ada migrasi
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

