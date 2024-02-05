package com.example.shopping_assistance.ui.account

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping_assistance.R
import com.example.shopping_assistance.ui.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class AccountFragment : Fragment() {

    private lateinit var textViewEmail: TextView
    private lateinit var recyclerViewOptions: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        textViewEmail = view.findViewById(R.id.textViewEmail)
        recyclerViewOptions = view.findViewById(R.id.recyclerViewOptions)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tutaj możesz dodać kod do ustawienia e-maila użytkownika
        textViewEmail.text = FirebaseAuth.getInstance().currentUser?.email

        // Następnie możesz skonfigurować RecyclerView z listą opcji
        val optionsList = listOf("Wyloguj się", "Inne opcje")
        val adapter = OptionsAdapter()
        adapter.submitList(optionsList)
        recyclerViewOptions.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewOptions.adapter = adapter
        // Obsługa kliknięcia na opcję
        adapter.setOnItemClickListener { position ->
            when (position) {
                0 -> logoutUser()
                // Dodaj inne obsługiwane opcje
            }
        }
    }

    private fun logoutUser() {
        // Wyloguj użytkownika z Firebase
        FirebaseAuth.getInstance().signOut()

        // Usuń dane logowania z SharedPreferences
        val sharedPreferences: SharedPreferences =
            activity?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE) ?: return
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // Przenieś użytkownika do LoginActivity
        val intent = Intent(activity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        activity?.finish()
    }
}