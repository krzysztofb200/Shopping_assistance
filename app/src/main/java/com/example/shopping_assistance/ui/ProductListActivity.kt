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
import com.example.shopping_assistance.ui.adapters.Product
import com.example.shopping_assistance.ui.adapters.ProductListAdapter
import com.example.shopping_assistance.ui.adapters.UsersListAdapter
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

        //Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = listName

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
            alertDialogBuilder.setTitle(R.string.delete_list)
            alertDialogBuilder.setMessage(R.string.do_you_want_to_delete_this_list)

            alertDialogBuilder.setPositiveButton(R.string.yes) { _, _ ->
                val email = FirebaseAuth.getInstance().currentUser?.email
                removeUserFromList(email.toString(), listId.toString())

                // Close the activity after deleting the list
                finish()
            }

            alertDialogBuilder.setNegativeButton(R.string.no) { _, _ ->
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

        //ItemTouchHelper
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
                // Add visual effect for swiping left
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

        // Download "products"  collection for a list
        firestore.collection("shoppingLists").document(listId).collection("products")
            .addSnapshotListener { value, exception ->
                if (exception != null) {
                    // Handling error downloading data from Firestore
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

                        // Read the values of the product
                        val productId = document.getString("productId") ?: ""
                        val name = document.getString("name") ?: ""
                        val purchased = document.getBoolean("purchased") ?: false

                        // Add product to the list
                        val product = Product(productId, name, purchased)
                        products.add(product)
                    }

                    // Check if products collection is empty
                    if (products.isEmpty()) {
                        // Products collection is empty, hide RecyclerView, show TextView
                        recyclerView.visibility = View.GONE
                        textNoProducts.visibility = View.VISIBLE
                    } else {
                        // Products collection is not empty, show RecyclerView, hide TextView
                        recyclerView.visibility = View.VISIBLE
                        textNoProducts.visibility = View.GONE

                        // Update the product list in the adapter
                        adapter.submitList(products)
                    }
                }
            }
    }

    private fun showAddProductDialog(listId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.add_product)

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton(R.string.add) { _, _ ->
            val productName = input.text.toString()

            if (productName.isNotEmpty()) {
                if (productName.length > 25) {
                    Toast.makeText(this, R.string.name_cant_be_longer_than, Toast.LENGTH_SHORT).show()
                } else {
                    addProductToFirestore(productName, listId)
                }
            } else {
                Toast.makeText(this, R.string.enter_the_product_name, Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun addProductToFirestore(productName: String, listId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val productId = UUID.randomUUID().toString()

        // Create a new product
        val newProduct = Product(productId, productName, purchased = false)

        // Add the product to the "products" collection in Firebase Firestore
        firestore.collection("shoppingLists").document(listId).collection("products").document(productId)
            .set(newProduct)
            .addOnSuccessListener {
                Toast.makeText(this, R.string.product_added_successfully, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Błąd podczas dodawania produktu do Firestore", e)
                Toast.makeText(this, R.string.error_while_adding_the_product, Toast.LENGTH_SHORT).show()
            }
    }

    private fun showUsersListDialog(listId: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Download the list of users for a specific shopping list
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

                showAlertDialog(usersList, listId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Błąd podczas pobierania listy użytkowników z Firestore", e)
                Toast.makeText(this, R.string.error_while_downloading_the_list, Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAlertDialog(usersList: List<String>, listId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.users_on_the_list)

        Log.d("ProductListActivity", "Liczba użytkowników: ${usersList.size}")

        val sortedList = usersList.sorted()

        val adapter = UsersListAdapter(this, sortedList, listId)
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

        builder.setAdapter(adapter) { _, position ->
            val userEmail = usersList[position]

            if (userEmail != currentUserEmail) {
                // Delete user on click only if it is not the currently logged in user
                Log.d(TAG, userEmail)

                removeUserFromList(userEmail, listId)
            } else {
                Toast.makeText(this, R.string.cant_delete_your_account, Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        alertDialog = builder.create()
        alertDialog?.show()
    }

    fun removeUserFromList(userEmail: String, listId: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Download a reference to the shopping list document
        val shoppingListDocRef = firestore.collection("shoppingLists").document(listId)

        // Remove a user from the "users" collection in the shopping list document
        shoppingListDocRef.collection("users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val userId = document.getString("userUid")
                    if (userId != null) {
                        shoppingListDocRef.collection("users").document(userId).delete()
                            .addOnSuccessListener {
                                // The user has been successfully deleted
                                Toast.makeText(this, R.string.user_deleted_from_the_list, Toast.LENGTH_SHORT).show()
                                removeListIdFromUserCollection(listId, userEmail)
                                alertDialog?.dismiss()
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Błąd podczas usuwania użytkownika", e)
                                Toast.makeText(this, R.string.error_while_deleting_the_user, Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Błąd podczas pobierania użytkownika do usunięcia", e)
                Toast.makeText(this, R.string.error_while_downloading_the_user, Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeListIdFromUserCollection(listId: String, userEmail: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Get user by "email" field in "userData" collection
        firestore.collection("userData")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val userUid = document.id // User found, get his uid
                    // Remove listId from the "lists" collection for a given user
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

        dialogBuilder.setTitle(R.string.add_user)
        dialogBuilder.setPositiveButton(R.string.add) { _, _ ->
            val email = emailEditText.text.toString()
            addUserToList(listId, email)
        }
        dialogBuilder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun addUserToList(listId: String, email: String) {
        // Checking whether the user with the given email address exists in the userData collection
        // Checking whether the user with the given email address does not already exist in the users subcollection of the current shopping list

        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("userData")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    // The user with the given email address exists, you can add it to the users subcollection
                    val userId = querySnapshot.documents[0].id

                    // Check if the user already exists in the users subcollection of the current list
                    firestore.collection("shoppingLists")
                        .document(listId)
                        .collection("users")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (!documentSnapshot.exists()) {
                                // Add the user to the users subcollection of the current list
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
                                                    val userUid = document.id

                                                    // Add the document to the "lists" collection for a given user
                                                    val listsCollectionRef = firestore.collection("userData")
                                                        .document(userUid)
                                                        .collection("lists")

                                                    // Create a document with the name listId and a listId field with the value listId
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
                                            R.string.user_added_to_the_shopping_list,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this,
                                            "${R.string.error_while_downloading_the_user}: $e",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } else {
                                // The user already exists in the users subcollection of the current list
                                Toast.makeText(
                                    this,
                                    R.string.user_already_exists_ont_the_list,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "${R.string.error_while_checking_the_user_data}: $e",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    // The user with the given email address does not exist in the userData collection
                    Toast.makeText(
                        this,
                        R.string.user_with_this_email_doesnt_exist,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "${R.string.error_while_checking_the_user_data}: $e",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}
