package com.example.powerbutton.com.example.powerbutton

import ButtonItem
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.powerbutton.R

class ButtonAdapter(
    private val context: Context,
    private val items: List<ButtonItem>,
    private val onClick: (ButtonItem) -> Unit
) : RecyclerView.Adapter<ButtonAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.icon)
        val label: TextView = view.findViewById(R.id.label)
        val container: View = icon.parent as View
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.button_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.icon.setImageResource(item.iconRes)
        holder.label.text = item.label
        holder.container.setBackgroundResource(item.backgroundResId)

        holder.itemView.setOnClickListener { onClick(item) }
    }
}
