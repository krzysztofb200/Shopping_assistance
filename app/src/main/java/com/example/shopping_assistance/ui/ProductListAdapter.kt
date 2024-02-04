package com.example.shopping_assistance.ui

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping_assistance.R
import com.google.firebase.firestore.FirebaseFirestore

data class Product(val productId: String, val name: String, val purchased: Boolean)

class ProductListAdapter(private val listId: String) : ListAdapter<Product, ProductListAdapter.ProductViewHolder>(ProductDiffCallback()), ItemTouchHelperListener {

    override fun submitList(list: List<Product>?) {
        super.submitList(list?.sortedWith(compareBy<Product> { it.purchased }.thenBy { it.name }))
    }

    // Implementacje funkcji adaptera, np. onCreateViewHolder, onBindViewHolder, itd.

    override fun onItemSwipeLeft(position: Int) {
        // Obsługa przesunięcia w lewo, np. usuwanie elementu
        // Usuń element z listy i odśwież RecyclerView
        val deletedProduct = getItem(position)
        Log.d(TAG, "ListId: $listId")

        // Usuń produkt z Firebase Firestore
        deleteProductFromFirestore(deletedProduct, listId)

        // Zaktualizuj listę i poinformuj adapter o zmianach
        val updatedList = currentList.toMutableList()
        updatedList.removeAt(position)
        submitList(updatedList)
    }

    override fun onItemSwipeRight(position: Int) {
        // Obsługa przesunięcia w prawo, np. zmiana wartości
        // Zmodyfikuj wartość elementu i odśwież RecyclerView
        val updatedProduct = getItem(position).copy(purchased = !getItem(position).purchased)

        // Aktualizuj wartość purchased w Firebase Firestore
        updateProductInFirestore(updatedProduct)

        // Zaktualizuj listę i poinformuj adapter o zmianach
        val updatedList = currentList.toMutableList()
        updatedList[position] = updatedProduct
        submitList(updatedList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productNameTextView: TextView = itemView.findViewById(R.id.productNameTextView)
        private val purchasedCheckBox: CheckBox = itemView.findViewById(R.id.purchasedCheckBox)

        fun bind(product: Product) {
            productNameTextView.text = product.name
            purchasedCheckBox.isChecked = product.purchased
        }
    }

    private class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    private fun deleteProductFromFirestore(product: Product, listId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val productId = product.productId

        // Uzyskaj referencję do dokumentu produktu w kolekcji "products"
        val productRef = firestore.collection("shoppingLists").document(listId).collection("products").document(productId)

        // Usuń produkt z kolekcji "products"
        productRef.delete()
            .addOnSuccessListener {
                Log.d(TAG, "Produkt usunięty z Firebase Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Błąd podczas usuwania produktu z Firebase Firestore", e)
            }
    }

    private fun updateProductInFirestore(product: Product) {
        val firestore = FirebaseFirestore.getInstance()
        val productId = product.productId

        // Uzyskaj referencję do dokumentu produktu w kolekcji "products"
        val productRef = firestore.collection("shoppingLists").document(listId).collection("products").document(productId)

        // Aktualizuj wartość purchased w dokumencie
        productRef.update("purchased", product.purchased)
            .addOnSuccessListener {
                Log.d(TAG, "Wartość purchased zaktualizowana w Firebase Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Błąd podczas aktualizacji wartości purchased w Firebase Firestore", e)
            }
    }
}