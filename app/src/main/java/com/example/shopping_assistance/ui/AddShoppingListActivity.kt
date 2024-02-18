package com.example.shopping_assistance.ui

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.shopping_assistance.R
import com.example.shopping_assistance.ui.models.ShoppingListClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

//import kotlinx.android.synthetic.main.activity_add_shopping_list.*

class AddShoppingListActivity : AppCompatActivity() {
    private lateinit var buttonAddList: Button
    private lateinit var editTextListName: EditText
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_shopping_list)

        // Inicjalizacja FirebaseAuth i FirebaseFirestore
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val email = firebaseAuth.currentUser?.email

        buttonAddList = findViewById(R.id.buttonAddList)
        editTextListName = findViewById(R.id.editTextListName)

        buttonAddList.setOnClickListener {
            val listName = editTextListName.text.toString()
            val currentUserUid = firebaseAuth.currentUser?.uid

            if (currentUserUid != null && listName.isNotBlank()) {
                // Utwórz obiekt ShoppingList
                val shoppingList = ShoppingListClass(
                     // Firestore automatycznie przypisze unikalny identyfikator
                    listName = listName
                )

                // Dodaj listę do Firestore
                addShoppingListToFirestore(shoppingList, currentUserUid, listName, email.toString())
            }
        }
    }
    private fun addShoppingListToFirestore(shoppingList: ShoppingListClass, userUid: String, listName: String, email: String) {

        firestore.collection("shoppingLists")
            .add(shoppingList)
            .addOnSuccessListener { documentReference ->
                // Dodano listę pomyślnie

                // Dodaj użytkownika do kolekcji 'users' dla danej listy
                val listId = documentReference.id
                val data = hashMapOf("listId" to listId,
                    "listName" to listName)
                firestore.collection("shoppingLists")
                    .document(listId).set(data)
                addCurrentUserToUsersCollection(listId, userUid, email.toString())
                addListToUserCollection(listId, userUid)

            }
            .addOnFailureListener { exception ->
                // Obsługa błędów dodawania do Firestore
                // Tutaj można dodać odpowiednie działania w przypadku błędu
            }
    }

    private fun addCurrentUserToUsersCollection(listId: String, userUid: String, email: String) {
        val userMap = hashMapOf("userUid" to userUid,
        "email" to email)

        firestore.collection("shoppingLists")
            .document(listId)
            .collection("users")
            .document(userUid)
            .set(userMap)
            .addOnSuccessListener {
                // Dodano użytkownika do kolekcji 'users' pomyślnie
                finish()
            }
            .addOnFailureListener { exception ->
                // Obsługa błędów dodawania do Firestore
                // Tutaj można dodać odpowiednie działania w przypadku błędu
            }
    }

    private fun addListToUserCollection(listId: String, userUid: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Utwórz mapę z danymi do dodania
        val data = hashMapOf(
            "listId" to listId
        )

        // Dodaj listId do kolekcji "userData" -> userUid -> "lists"
        firestore.collection("userData").document(userUid).collection("lists")
            .document(listId)
            .set(data)
            .addOnSuccessListener {
                Log.d(TAG, "Dodano listId do kolekcji użytkownika: $listId")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Błąd podczas dodawania listId do kolekcji użytkownika", exception)
            }
    }


}
