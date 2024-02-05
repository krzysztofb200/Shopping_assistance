package com.example.shopping_assistance.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.shopping_assistance.R
import com.google.android.material.switchmaterial.SwitchMaterial

class ChangeThemeActivity : AppCompatActivity() {
    private lateinit var switchDarkMode: SwitchMaterial
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_theme)

        switchDarkMode = findViewById(R.id.switchDarkMode)
        sharedPreferences = getSharedPreferences("themePrefs", MODE_PRIVATE)

        // Ustawienie aktualnego stanu switcha zgodnie ze stanem motywu
        switchDarkMode.isChecked = sharedPreferences.getBoolean("isDarkMode", false)

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // Zapisz stan do SharedPreferences
            with(sharedPreferences.edit()) {
                putBoolean("isDarkMode", isChecked)
                apply()
            }

            // Zastosuj zmiany w motywie
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
}