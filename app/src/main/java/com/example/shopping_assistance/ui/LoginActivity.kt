package com.example.shopping_assistance.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.shopping_assistance.MainActivity
import com.example.shopping_assistance.R
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicjalizacja FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        setTheme()

        // Inicjalizacja SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Sprawdzanie, czy użytkownik jest już zalogowany
//        if (firebaseAuth.currentUser != null) {
//            redirectToMainActivity()
//        }
        if(sharedPreferences.contains("login") && sharedPreferences.contains("password")){
            loginUser(sharedPreferences.getString("login", "x").toString(),
                sharedPreferences.getString("password", "x").toString()
            )
            redirectToMainActivity()
        }

        // Inicjalizacja widoków
        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.buttonLogin)
        registerButton = findViewById(R.id.buttonRegister)

        // Obsługa przycisku logowania
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Wprowadź email i hasło", Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener{
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
            finish()
        }

        //TODO: password recovery function
    }

    private fun setTheme() {
        sharedPreferences = getSharedPreferences("themePrefs", Context.MODE_PRIVATE)

        // Pobierz aktualny stan isDarkMode z SharedPreferences
        val isDarkMode = sharedPreferences.getBoolean("isDarkMode", false)

        // Ustaw motyw zgodnie ze stanem isDarkMode
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun loginUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Logowanie zakończone sukcesem
                    Toast.makeText(this, "Logowanie udane", Toast.LENGTH_SHORT).show()

                    // Zapisz informację o zalogowanym użytkowniku w SharedPreferences
                    saveLoginStatus(email, password)

                    // Przekieruj do innej aktywności lub ekranu
                    redirectToMainActivity()
                } else {
                    // Logowanie nieudane
                    Toast.makeText(this, "Logowanie nieudane", Toast.LENGTH_SHORT).show()
                    Log.e("LoginActivity", "Błąd logowania: ${task.exception?.message}")
                }
            }
    }

    private fun saveLoginStatus(login: String, password: String) {
        val editor = sharedPreferences.edit()
        editor.putString("login", login)
        editor.putString("password", password)
        editor.apply()
    }

    private fun redirectToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}