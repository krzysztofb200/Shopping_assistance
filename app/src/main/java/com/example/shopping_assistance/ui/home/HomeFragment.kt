package com.example.shopping_assistance.ui.home

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping_assistance.R
import com.example.shopping_assistance.databinding.FragmentHomeBinding
import com.example.shopping_assistance.ui.AddShoppingListActivity
import com.example.shopping_assistance.ui.adapters.ShoppingListAdapter
import com.example.shopping_assistance.ui.models.ShoppingListClass
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var progressBar: ProgressBar

    private lateinit var fabAddList: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var noListsTextView: TextView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: ShoppingListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        progressBar = view.findViewById(R.id.progressBar)

        // firebase init
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        Log.d("UID", firebaseAuth.uid.toString())

        // views init
        fabAddList = view.findViewById(R.id.fabAddList)
        recyclerView = view.findViewById(R.id.recyclerViewLists)
        noListsTextView = view.findViewById(R.id.textViewNoLists)

        // recyclerview init
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ShoppingListAdapter()
        recyclerView.adapter = adapter

        fabAddList.setOnClickListener {
            val intent = Intent(requireContext(), AddShoppingListActivity::class.java)
            startActivity(intent)
        }

        loadShoppingLists()

        return view
    }

    private fun loadShoppingLists() {
        progressBar.visibility = View.VISIBLE

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Check if the user is logged in
        if (currentUserUid != null) {
            val firestore = FirebaseFirestore.getInstance()

            // Download lists from "userData"
            firestore.collection("userData")
                .document(currentUserUid)
                .collection("lists")
                .get()
                .addOnSuccessListener { listsSnapshot ->
                    // Check if there are any lists
                    if (listsSnapshot.isEmpty) {
                        Log.d("list empty", "List empty")
                        // If no lists
                        recyclerView.visibility = View.GONE
                        noListsTextView.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                    } else {
                        // There are some lists
                        recyclerView.visibility = View.VISIBLE
                        noListsTextView.visibility = View.GONE

                        val shoppingListsIds = mutableListOf<String>()

                        // Retrieve lists from the "shoppingLists" collection based on listId
                        for (listDocument in listsSnapshot.documents) {
                            val listId = listDocument.getString("listId") ?: ""
                            shoppingListsIds.add(listId)
                        }

                        // Download shopping lists for a user from the "shoppingLists" collection
                        firestore.collection("shoppingLists")
                            .whereIn("listId", shoppingListsIds)
                            .get()
                            .addOnSuccessListener { shoppingListsSnapshot ->
                                val shoppingLists = mutableListOf<ShoppingListClass>()

                                for (shoppingListDocument in shoppingListsSnapshot.documents) {
                                    // Read the data of a shopping list
                                    val listName = shoppingListDocument.getString("listName") ?: ""
                                    val listId = shoppingListDocument.id

                                    // Add list to shopping list
                                    val shoppingList = ShoppingListClass(listId, listName)
                                    shoppingLists.add(shoppingList)
                                }

                                progressBar.visibility = View.GONE

                                // Update the list in the adapter
                                shoppingLists.sortBy { it.listName.lowercase() }
                                adapter.submitList(shoppingLists)
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "Błąd pobierania list zakupowych", exception)
                                progressBar.visibility = View.GONE
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Błąd pobierania list z kolekcji userData", exception)
                    progressBar.visibility = View.GONE
                }
        } else {
            // The user isn't logged in
            recyclerView.visibility = View.GONE
            noListsTextView.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("FragmentLifecycle", "onResume")
        loadShoppingLists()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}