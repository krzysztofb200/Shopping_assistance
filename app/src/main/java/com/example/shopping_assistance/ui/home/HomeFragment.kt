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
import com.example.shopping_assistance.ui.ShoppingListAdapter
import com.example.shopping_assistance.ui.ShoppingListClass
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

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

        // Inicjalizacja FirebaseAuth i FirebaseFirestore
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        Log.d("UID", firebaseAuth.uid.toString())

        // Inicjalizacja widoków
        fabAddList = view.findViewById(R.id.fabAddList)
        recyclerView = view.findViewById(R.id.recyclerViewLists)
        noListsTextView = view.findViewById(R.id.textViewNoLists)

        // Konfiguracja RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ShoppingListAdapter()
        recyclerView.adapter = adapter

        // Ustawienie listenera dla FAB
        fabAddList.setOnClickListener {
            val intent = Intent(requireContext(), AddShoppingListActivity::class.java)
            startActivity(intent)
        }

        // Pobierz i wyświetl listy zakupów użytkownika
        loadShoppingLists()

        return view
    }

    private fun loadShoppingLists() {
        // Pokaż ProgressBar podczas ładowania
        progressBar.visibility = View.VISIBLE

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Sprawdź, czy aktualny użytkownik jest zalogowany
        if (currentUserUid != null) {
            val firestore = FirebaseFirestore.getInstance()

            // Pobierz listy użytkownika z kolekcji "userData"
            firestore.collection("userData")
                .document(currentUserUid)
                .collection("lists")
                .get()
                .addOnSuccessListener { listsSnapshot ->
                    // Sprawdź, czy są jakiekolwiek listy dla użytkownika
                    if (listsSnapshot.isEmpty) {
                        Log.d("list empty", "List empty")
                        // Brak list, ukryj RecyclerView, pokaż TextView
                        recyclerView.visibility = View.GONE
                        noListsTextView.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                    } else {
                        // Są listy, ukryj TextView, pokaż RecyclerView
                        recyclerView.visibility = View.VISIBLE
                        noListsTextView.visibility = View.GONE

                        val shoppingListsIds = mutableListOf<String>()

                        // Pobierz listy z kolekcji "shoppingLists" na podstawie listId
                        for (listDocument in listsSnapshot.documents) {
                            val listId = listDocument.getString("listId") ?: ""
                            shoppingListsIds.add(listId)
                        }

                        // Pobierz listy zakupowe dla użytkownika z kolekcji "shoppingLists"
                        firestore.collection("shoppingLists")
                            .whereIn("listId", shoppingListsIds)
                            .get()
                            .addOnSuccessListener { shoppingListsSnapshot ->
                                val shoppingLists = mutableListOf<ShoppingListClass>()

                                for (shoppingListDocument in shoppingListsSnapshot.documents) {
                                    // Odczytaj dane listy zakupowej
                                    val listName = shoppingListDocument.getString("listName") ?: ""
                                    val listId = shoppingListDocument.id

                                    // Dodaj listę do listy zakupowej
                                    val shoppingList = ShoppingListClass(listId, listName)
                                    shoppingLists.add(shoppingList)
                                }

                                // Ukryj ProgressBar po zakończeniu ładowania
                                progressBar.visibility = View.GONE

                                // Zaktualizuj listę zakupową w adapterze
                                shoppingLists.sortBy { it.listName.lowercase() }
                                adapter.submitList(shoppingLists)
                            }
                            .addOnFailureListener { exception ->
                                // Obsługa błędu pobierania list zakupowych
                                Log.e(TAG, "Błąd pobierania list zakupowych", exception)
                                // Ukryj ProgressBar po zakończeniu ładowania
                                progressBar.visibility = View.GONE
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    // Obsługa błędu pobierania list z kolekcji "userData"
                    Log.e(TAG, "Błąd pobierania list z kolekcji userData", exception)
                    // Ukryj ProgressBar po zakończeniu ładowania
                    progressBar.visibility = View.GONE
                }
        } else {
            // Aktualny użytkownik niezalogowany, ukryj RecyclerView, pokaż TextView
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