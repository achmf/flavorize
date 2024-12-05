package com.example.flavorize.ui.activities.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatDelegate
import com.example.flavorize.R
import com.google.android.material.materialswitch.MaterialSwitch

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Setup Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_arrow)
            title = "Settings"
        }

        val themeSwitch = findViewById<MaterialSwitch>(R.id.themeSwitch)

        // SharedPreferences for theme state persistence
        val sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        // Load current theme state
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        themeSwitch.isChecked = isDarkMode

        // Update theme based on switch toggle
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                editor.putBoolean("dark_mode", true)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                editor.putBoolean("dark_mode", false)
            }
            editor.apply()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
