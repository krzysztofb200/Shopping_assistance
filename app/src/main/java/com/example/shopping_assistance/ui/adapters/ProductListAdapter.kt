package com.example.shopping_assistance.ui.adapters

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
import com.example.shopping_assistance.ui.ItemTouchHelperListener
import com.google.firebase.firestore.FirebaseFirestore

data class Product(val productId: String, val name: String, val purchased: Boolean)

class ProductListAdapter(private val listId: String) : ListAdapter<Product, ProductListAdapter.ProductViewHolder>(
    ProductDiffCallback()
), ItemTouchHelperListener {

    override fun submitList(list: List<Product>?) {
        super.submitList(list?.sortedWith(compareBy<Product> { it.purchased }.thenBy { it.name }))
    }

    override fun onItemSwipeLeft(position: Int) {
        // Delete item from the list and refresh recyclerview
        val deletedProduct = getItem(position)
        Log.d(TAG, "ListId: $listId")

        // Delete product from firestore
        deleteProductFromFirestore(deletedProduct, listId)

        // Update the list
        val updatedList = currentList.toMutableList()
        updatedList.removeAt(position)
        submitList(updatedList)
    }

    override fun onItemSwipeRight(position: Int) {
        // Update the item and refresh recyclreview
        val updatedProduct = getItem(position).copy(purchased = !getItem(position).purchased)

        // Update the "purchased" value
        updateProductInFirestore(updatedProduct)

        // Update the list
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

        val productRef = firestore.collection("shoppingLists").document(listId).collection("products").document(productId)

        // Delete product from products collection
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

        val productRef = firestore.collection("shoppingLists").document(listId).collection("products").document(productId)

        productRef.update("purchased", product.purchased)
            .addOnSuccessListener {
                Log.d(TAG, "Wartość purchased zaktualizowana w Firebase Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Błąd podczas aktualizacji wartości purchased w Firebase Firestore", e)
            }
    }
}
