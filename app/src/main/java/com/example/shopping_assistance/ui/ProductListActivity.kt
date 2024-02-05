package com.example.shopping_assistance.ui

import android.content.ContentValues.TAG
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping_assistance.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class ProductListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var textNoProducts: TextView
    private lateinit var adapter: ProductListAdapter
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        val listName = intent.getStringExtra("listName")
        val listId = intent.getStringExtra("listId")

        // Inicjalizacja Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = listName

        // Inicjalizacja ImageView z ikoną użytkownika
        val imageViewUser: ImageView = findViewById(R.id.imageViewUser)
        imageViewUser.setOnClickListener {
            showUsersListDialog(listId.toString())
        }

        val addUserImageView: ImageView = findViewById(R.id.imageViewAddUser)
        addUserImageView.setOnClickListener {
            showAddUserDialog(listId.toString())
        }

        val imageDeleleteList: ImageView = findViewById(R.id.imageDeleteList)
        imageDeleleteList.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Usuń listę")
            alertDialogBuilder.setMessage("Czy na pewno chcesz usunąć tę listę?")

            alertDialogBuilder.setPositiveButton("Tak") { _, _ ->
                // Usunięcie listy i inne operacje, które mają być wykonane po potwierdzeniu
                val email = FirebaseAuth.getInstance().currentUser?.email
                removeUserFromList(email.toString(), listId.toString())

                // Zamknięcie aktywności po usunięciu listy
                finish()
            }

            alertDialogBuilder.setNegativeButton("Nie") { _, _ ->
                // Operacje po anulowaniu usunięcia
            }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        recyclerView = findViewById(R.id.recyclerView)
        adapter = ProductListAdapter(listId.toString())

        textNoProducts = findViewById<TextView>(R.id.textViewNoProducts)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val fabAddProduct: FloatingActionButton = findViewById(R.id.fabAddProduct)
        fabAddProduct.setOnClickListener {
            showAddProductDialog(listId.toString())
        }

        // Inicjalizacja ItemTouchHelper
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                when (direction) {
                    ItemTouchHelper.LEFT -> adapter.onItemSwipeLeft(position)
                    ItemTouchHelper.RIGHT -> adapter.onItemSwipeRight(position)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                // Dodaj efekt wizualny dla przesunięcia w lewo
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
                    val itemView = viewHolder.itemView
                    val background = ColorDrawable(Color.RED)
                    background.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    background.draw(c)
                }

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX > 0) {
                    val itemView = viewHolder.itemView
                    val background = ColorDrawable(Color.GREEN)
                    background.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.left + dX.toInt(),
                        itemView.bottom
                    )
                    background.draw(c)
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }


        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        if (listId != null) {
            loadProducts(listId)
        }
    }

    private fun loadProducts(listId: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Pobierz kolekcję "products" dla danej listy
        firestore.collection("shoppingLists").document(listId).collection("products")
            .addSnapshotListener { value, exception ->
                if (exception != null) {
                    // Obsługa błędu pobierania danych z Firestore
                    Log.e(TAG, "Błąd pobierania danych z Firestore", exception)
                    return@addSnapshotListener
                }

                if (value != null) {
                    Log.d(TAG, "Pobrano dokument z kolekcji products: ${value}")
                    val products = mutableListOf<Product>()

                    for (document in value.documents) {
                        if (document.id == "exists") {
                            continue
                        }

                        // Odczytaj dane produktu z dokumentu
                        val productId = document.getString("productId") ?: ""
                        val name = document.getString("name") ?: ""
                        val purchased = document.getBoolean("purchased") ?: false

                        // Dodaj produkt do listy
                        val product = Product(productId, name, purchased)
                        products.add(product)
                    }

                    // Sprawdź, czy kolekcja products jest pusta
                    if (products.isEmpty()) {
                        // Kolekcja products jest pusta, ukryj RecyclerView, pokaż TextView
                        recyclerView.visibility = View.GONE
                        textNoProducts.visibility = View.VISIBLE
                    } else {
                        // Kolekcja products nie jest pusta, pokaż RecyclerView, ukryj TextView
                        recyclerView.visibility = View.VISIBLE
                        textNoProducts.visibility = View.GONE

                        // Zaktualizuj listę produktów w adapterze
                        adapter.submitList(products)
                    }
                }
            }
    }


    private fun showAddProductDialog(listId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Dodaj produkt")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("Dodaj") { _, _ ->
            val productName = input.text.toString()

            if (productName.isNotEmpty()) {
                if (productName.length > 25) {
                    Toast.makeText(this, "Nazwa produktu nie może być dłuższa niż 25 znaków", Toast.LENGTH_SHORT).show()
                } else {
                    addProductToFirestore(productName, listId)
                }
            } else {
                Toast.makeText(this, "Podaj nazwę produktu", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun addProductToFirestore(productName: String, listId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val productId = UUID.randomUUID().toString()

        // Utwórz nowy produkt
        val newProduct = Product(productId, productName, purchased = false)

        // Dodaj produkt do kolekcji "products" w Firebase Firestore
        firestore.collection("shoppingLists").document(listId).collection("products").document(productId)
            .set(newProduct)
            .addOnSuccessListener {
                Toast.makeText(this, "Produkt dodany pomyślnie", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Błąd podczas dodawania produktu do Firestore", e)
                Toast.makeText(this, "Błąd dodawania produktu", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showUsersListDialog(listId: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Pobierz listę użytkowników dla konkretnej listy zakupowej
        firestore.collection("shoppingLists").document(listId).collection("users")
            .get()
            .addOnSuccessListener { result ->
                val usersList = mutableListOf<String>()

                for (document in result) {
                    val email = document.getString("email")
                    if (email != null) {
                        usersList.add(email)
                    }
                }

                Log.d("ProductListActivity", "Liczba użytkowników: ${usersList.size}")

                // Wyświetl listę użytkowników w AlertDialog
                showAlertDialog("Użytkownicy na liście", usersList, listId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Błąd podczas pobierania listy użytkowników z Firestore", e)
                Toast.makeText(this, "Błąd pobierania listy użytkowników", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAlertDialog(title: String, usersList: List<String>, listId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)

        Log.d("ProductListActivity", "Liczba użytkowników: ${usersList.size}")

        val sortedList = usersList.sorted()

        val adapter = UsersListAdapter(this, sortedList, listId)  // Zmiana na this jako kontekst
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email // Zastąp to odpowiednim kodem do pobrania aktualnie zalogowanego użytkownika

        builder.setAdapter(adapter) { _, position ->
            val userEmail = usersList[position]

            if (userEmail != currentUserEmail) {
                // Usuń użytkownika po kliknięciu tylko jeśli to nie jest aktualnie zalogowany użytkownik
                Log.d(TAG, userEmail)

                removeUserFromList(userEmail, listId)
            } else {
                // To jest aktualnie zalogowany użytkownik, możesz dodać odpowiednią obsługę (np. komunikat, że nie można usunąć samego siebie)
                Toast.makeText(this, "Nie możesz usunąć samego siebie", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.cancel()
        }

        alertDialog = builder.create()  // Przypisz utworzoną instancję do zmiennej alertDialog
        alertDialog?.show()
    }

    fun removeUserFromList(userEmail: String, listId: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Pobierz referencję do dokumentu listy zakupowej
        val shoppingListDocRef = firestore.collection("shoppingLists").document(listId)

        // Usuń użytkownika z kolekcji "users" w dokumencie listy zakupowej
        shoppingListDocRef.collection("users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    // Znaleziono dokument, który chcemy usunąć
                    val userId = document.getString("userUid")
                    if (userId != null) {
                        shoppingListDocRef.collection("users").document(userId).delete()
                            .addOnSuccessListener {
                                // Użytkownik został pomyślnie usunięty
                                Toast.makeText(this, "Użytkownik usunięty z listy", Toast.LENGTH_SHORT).show()
                                removeListIdFromUserCollection(listId, userEmail)
                                alertDialog?.dismiss()
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Błąd podczas usuwania użytkownika", e)
                                Toast.makeText(this, "Błąd usuwania użytkownika", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Błąd podczas pobierania użytkownika do usunięcia", e)
                Toast.makeText(this, "Błąd pobierania użytkownika do usunięcia", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeListIdFromUserCollection(listId: String, userEmail: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Pobierz użytkownika po polu "email" w kolekcji "userData"
        firestore.collection("userData")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val userUid = document.id // Odnajdziono użytkownika, pobierz jego uid
                    // Usuń listId z kolekcji "lists" dla danego użytkownika
                    firestore.collection("userData").document(userUid).collection("lists")
                        .document(listId)
                        .delete()
                        .addOnSuccessListener {
                            Log.d(TAG, "Usunięto listId z kolekcji użytkownika: $listId")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Błąd podczas usuwania listId z kolekcji użytkownika", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Błąd podczas pobierania użytkownika do usunięcia listId", e)
            }
    }

    private fun showAddUserDialog(listId: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_user, null)
        dialogBuilder.setView(dialogView)

        val emailEditText: EditText = dialogView.findViewById(R.id.editTextEmail)

        dialogBuilder.setTitle("Dodaj użytkownika")
        dialogBuilder.setPositiveButton("Dodaj") { _, _ ->
            val email = emailEditText.text.toString()
            addUserToList(listId, email)
        }
        dialogBuilder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun addUserToList(listId: String, email: String) {
        // Sprawdź, czy użytkownik o podanym email istnieje w kolekcji userData
        // Sprawdź, czy użytkownik o podanym email już nie istnieje w subkolekcji users aktualnej listy zakupowej

        // Dodaj odpowiednie operacje Firebase Firestore, aby sprawdzić i dodać użytkownika

        // Przykładowa operacja dodawania użytkownika do subkolekcji users aktualnej listy
        // pobierz UID aktualnie zalogowanego użytkownika

        val firestore = FirebaseFirestore.getInstance()

        // Sprawdź, czy użytkownik o podanym email istnieje w kolekcji userData
        firestore.collection("userData")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    // Użytkownik o podanym email istnieje, możesz dodać go do subkolekcji users
                    val userId = querySnapshot.documents[0].id

                    // Sprawdź, czy użytkownik już nie istnieje w subkolekcji users aktualnej listy
                    firestore.collection("shoppingLists")
                        .document(listId)
                        .collection("users")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (!documentSnapshot.exists()) {
                                // Dodaj użytkownika do subkolekcji users aktualnej listy
                                firestore.collection("shoppingLists")
                                    .document(listId)
                                    .collection("users")
                                    .document(userId)
                                    .set(mapOf("userUid" to userId,
                                        "email" to email))
                                    .addOnSuccessListener {
                                        firestore.collection("userData")
                                            .whereEqualTo("email", email)
                                            .get()
                                            .addOnSuccessListener { result ->
                                                for (document in result) {
                                                    val userUid = document.id // Odnajdziono użytkownika, pobierz jego uid

                                                    // Dodaj dokument do kolekcji "lists" dla danego użytkownika
                                                    val listsCollectionRef = firestore.collection("userData")
                                                        .document(userUid)
                                                        .collection("lists")

                                                    // Utwórz dokument o nazwie listId i polu listId o wartości listId
                                                    listsCollectionRef.document(listId)
                                                        .set(mapOf("listId" to listId))
                                                        .addOnSuccessListener {
                                                            Log.d(TAG, "Dodano listId do kolekcji użytkownika: $listId")
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Log.e(TAG, "Błąd podczas dodawania listId do kolekcji użytkownika", e)
                                                        }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(TAG, "Błąd podczas pobierania użytkownika do dodania listId", e)
                                            }
                                        Toast.makeText(
                                            this,
                                            "Użytkownik dodany do listy zakupowej",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this,
                                            "Błąd dodawania użytkownika: $e",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } else {
                                // Użytkownik już istnieje w subkolekcji users aktualnej listy
                                Toast.makeText(
                                    this,
                                    "Użytkownik już istnieje na liście zakupowej",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Błąd sprawdzania użytkownika: $e",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    // Użytkownik o podanym email nie istnieje w kolekcji userData
                    Toast.makeText(
                        this,
                        "Użytkownik o podanym email nie istnieje",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Błąd sprawdzania użytkownika: $e",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}
