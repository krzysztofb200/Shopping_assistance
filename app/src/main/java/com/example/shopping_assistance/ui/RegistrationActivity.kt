package com.example.shopping_assistance.ui

import android.content.Intent
import android.os.Bundle
import com.example.shopping_assistance.R
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shopping_assistance.MainActivity
import com.google.firebase.auth.FirebaseAuth

class RegistrationActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Inicjalizacja FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Inicjalizacja widoków
        emailEditText = findViewById(R.id.editTextEmailRegistration)
        passwordEditText = findViewById(R.id.editTextPasswordRegistration)
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword)
        registerButton = findViewById(R.id.buttonRegister)

        // Obsługa przycisku rejestracji
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password.length >= 8) {
                    if (password == confirmPassword) {
                        registerUser(email, password)
                    } else {
                        Toast.makeText(this, "Hasła nie są identyczne", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Hasło musi mieć co najmniej 8 znaków", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Wprowadź email, hasło i potwierdź hasło", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Rejestracja zakończona sukcesem
                    Toast.makeText(this, "Rejestracja udana", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Rejestracja nieudana
                    Toast.makeText(this, "Rejestracja nieudana", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
