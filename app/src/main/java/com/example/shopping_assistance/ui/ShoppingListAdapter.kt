package com.example.shopping_assistance.ui

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping_assistance.R

class ShoppingListAdapter : ListAdapter<ShoppingListClass, ShoppingListAdapter.ShoppingListViewHolder>(ShoppingListDiffCallback()) {

//    override fun submitList(list: List<ShoppingListClass>?) {
//        super.submitList(list?.sortedWith(compareBy { it.listName.lowercase() }))
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_shopping_list, parent, false)
        return ShoppingListViewHolder(view)
        //TODO: Listy zakupów wyświetlają się w alfabetycznej kolejności ale kliknięcie w którąś z
        // nich nie koniecznie prowadzi do jej odpowiednika w bazie danych
    }

    override fun onBindViewHolder(holder: ShoppingListViewHolder, position: Int) {
        val shoppingList = getItem(position)
        holder.bind(shoppingList)
        holder.itemView.setOnClickListener {
            // Pobierz klikniętą listę
            val clickedShoppingList = getItem(position)
            Log.d("listId", clickedShoppingList.listId)

            // Przekieruj do nowej aktywności, przekazując ID lub inne informacje o liście
            val intent = Intent(holder.itemView.context, ProductListActivity::class.java)
            intent.putExtra("listId", clickedShoppingList.listId)
            intent.putExtra("listName", clickedShoppingList.listName)
            holder.itemView.context.startActivity(intent)
        }
    }

    inner class ShoppingListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val listNameTextView: TextView = itemView.findViewById(R.id.textViewListName)

        fun bind(shoppingList: ShoppingListClass) {
            listNameTextView.text = shoppingList.listName
            //TODO: Add onClick feature
            // Tutaj można dodać dodatkowe operacje, np. obsługę kliknięcia na element
        }
    }

    private class ShoppingListDiffCallback : DiffUtil.ItemCallback<ShoppingListClass>() {
        override fun areItemsTheSame(oldItem: ShoppingListClass, newItem: ShoppingListClass): Boolean {
            return oldItem.listId == newItem.listId
        }

        override fun areContentsTheSame(oldItem: ShoppingListClass, newItem: ShoppingListClass): Boolean {
            //return oldItem == newItem
            //Jak jest dane return false, to lista się cała odświeża po usunięciu lub dodaniu jakiegoś
            //elementu, a nie tylko jeden jej element
            return false
        }
    }
}
