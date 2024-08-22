package com.simplifiedkiosk.ui.itemlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simplifiedkiosk.R
import com.simplifiedkiosk.model.Item
import com.squareup.picasso.Picasso

class ItemAdapter(
    private val onItemClicked: (Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private var items: List<Item> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_row, parent, false)
        return ItemViewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        // Bind data to the views here
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ItemViewHolder(
        view: View,
        private val onItemClicked: (Item) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        // Initialize views from the layout
        val avatarImg = view.findViewById<ImageView>(R.id.imageView)
        val itemName = view.findViewById<TextView>(R.id.textViewName)
        val itemDescription = view.findViewById<TextView>(R.id.textViewDescription)
        val itemPrice = view.findViewById<TextView>(R.id.textViewPrice)

        fun bind(item: Item){
            itemView.setOnClickListener { onItemClicked(item) }
            Picasso.get().load(item.imageUrl).into(avatarImg)
            itemName.text = item.name
            itemDescription.text = item.description
            itemPrice.text = item.price.toString()
        }
    }
}
