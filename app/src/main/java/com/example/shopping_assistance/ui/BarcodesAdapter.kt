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

class BarcodesAdapter : ListAdapter<BarcodeClass, BarcodesAdapter.BarcodeViewHolder>(BarcodeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarcodeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_barcode, parent, false)
        return BarcodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BarcodeViewHolder, position: Int) {
        val barcode = getItem(position)
        holder.bind(barcode)
        holder.itemView.setOnClickListener {
            // Pobierz klikniętą listę
            val clickedBarcode = getItem(position)
            Log.d("listId", clickedBarcode.id)

            // Przekieruj do nowej aktywności, przekazując ID lub inne informacje o liście
            val intent = Intent(holder.itemView.context, ShowBarcodeActivity::class.java)
            intent.putExtra("barcodeId", clickedBarcode.id)
            intent.putExtra("barcodeName", clickedBarcode.name)
            intent.putExtra("barcodeValue", clickedBarcode.value)
            holder.itemView.context.startActivity(intent)
        }
    }

    class BarcodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textViewBarcodeName)
        private val valueTextView: TextView = itemView.findViewById(R.id.textViewBarcodeValue)

        fun bind(barcode: BarcodeClass) {
            nameTextView.text = barcode.name
            valueTextView.text = barcode.value
        }
    }

    private class BarcodeDiffCallback : DiffUtil.ItemCallback<BarcodeClass>() {
        override fun areItemsTheSame(oldItem: BarcodeClass, newItem: BarcodeClass): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BarcodeClass, newItem: BarcodeClass): Boolean {
            return oldItem == newItem
        }
    }
}