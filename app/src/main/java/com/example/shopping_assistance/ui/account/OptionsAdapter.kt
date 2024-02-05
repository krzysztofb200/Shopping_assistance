package com.example.shopping_assistance.ui.account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping_assistance.R

class OptionsAdapter : ListAdapter<String, OptionsAdapter.OptionsViewHolder>(OptionsDiffCallback()) {

    private var listener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_option, parent, false)
        return OptionsViewHolder(view)
    }

    override fun onBindViewHolder(holder: OptionsViewHolder, position: Int) {
        val option = getItem(position)
        holder.bind(option)

        holder.itemView.setOnClickListener {
            listener?.invoke(position)
        }
    }

    inner class OptionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewOption: TextView = itemView.findViewById(R.id.textViewOption)

        fun bind(option: String) {
            textViewOption.text = option
        }
    }

    private class OptionsDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}