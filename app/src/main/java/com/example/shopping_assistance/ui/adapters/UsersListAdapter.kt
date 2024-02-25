package com.example.shopping_assistance.ui.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.shopping_assistance.R
import com.example.shopping_assistance.ui.ProductListActivity
import com.google.firebase.auth.FirebaseAuth

class UsersListAdapter(private val context: Context, private val usersList: List<String>, private val listId: String) :
    ArrayAdapter<String>(context, 0, usersList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val userEmail = getItem(position)
        val actualEmail = FirebaseAuth.getInstance().currentUser?.email

        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_user_list, parent, false)

        val textViewUserEmail: TextView = view.findViewById(R.id.textViewUserEmail)
        val imageViewDeleteUser: ImageView = view.findViewById(R.id.imageViewDeleteUser)

        Log.d("UsersListAdapter", "Email: $userEmail")  // Dodaj logi debugowania

        textViewUserEmail.text = userEmail

        if (userEmail == actualEmail) {
            // Hide the trash icon for actual user
            imageViewDeleteUser.visibility = View.GONE
        } else {
            imageViewDeleteUser.visibility = View.VISIBLE

            imageViewDeleteUser.setOnClickListener {
                // Delete user after clicking
                if (userEmail != null) {
                    (context as ProductListActivity).removeUserFromList(userEmail, listId)
                }
            }
        }

        return view
    }

}
