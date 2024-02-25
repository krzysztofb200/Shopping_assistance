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
import com.example.shopping_assistance.ui.ChangeThemeActivity
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

        textViewEmail.text = FirebaseAuth.getInstance().currentUser?.email

        val optionsList = listOf(getString(R.string.log_out), getString(R.string.theme_mode))
        val adapter = OptionsAdapter()
        adapter.submitList(optionsList)
        recyclerViewOptions.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewOptions.adapter = adapter
        adapter.setOnItemClickListener { position ->
            when (position) {
                0 -> logoutUser()
                1 -> changeTheme()
            }
        }
    }

    private fun logoutUser() {
        // Logging out
        FirebaseAuth.getInstance().signOut()

        // Deleting data from SharedPreferences
        val sharedPreferences: SharedPreferences =
            activity?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE) ?: return
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // Go to LoginActivity
        val intent = Intent(activity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        activity?.finish()
    }

    private fun changeTheme(){
        val intent = Intent(activity, ChangeThemeActivity::class.java)
        startActivity(intent)
    }
}