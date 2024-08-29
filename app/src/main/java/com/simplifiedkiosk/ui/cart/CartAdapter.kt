package com.simplifiedkiosk.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.simplifiedkiosk.R
import com.simplifiedkiosk.model.CartItem
import com.simplifiedkiosk.model.Product

class CartAdapter(private val onItemClick: (Product) -> Unit) : ListAdapter<Product, CartAdapter.CartViewHolder>(CartItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_item_row, parent, false)
        return CartViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = getItem(position)
        holder.bind(cartItem)
    }

    class CartViewHolder(itemView: View, private val onItemClick: (Product) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val itemNameTextView: TextView = itemView.findViewById(R.id.cartItemNameTextView)
        private val itemQuantityTextView: TextView = itemView.findViewById(R.id.cartItemQuantityTextView)
        private val itemPriceTextView: TextView = itemView.findViewById(R.id.cartItemPriceTextView)

        fun bind(cartItem: Product) {
            itemNameTextView.text = cartItem.title
            itemQuantityTextView.text = "Qty: ${cartItem.quantity}"
            if(cartItem.price != null && cartItem.quantity != null){
                cartItem.quantity?.let {
                    val totalPrice = cartItem.price.toDouble() * it
                    itemPriceTextView.text = "$$totalPrice"
                }
            } else {
                itemPriceTextView.text = "$0.00"
            }
            itemView.setOnClickListener {
                onItemClick.invoke(cartItem)
            }
        }
    }
}

class CartItemDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.dbId == newItem.dbId
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}
