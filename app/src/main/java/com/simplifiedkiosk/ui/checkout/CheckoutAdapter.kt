package com.simplifiedkiosk.ui.checkout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.simplifiedkiosk.R
import com.simplifiedkiosk.model.CartItem

class CheckoutAdapter : ListAdapter<CartItem, CheckoutAdapter.CheckoutViewHolder>(CheckoutItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_item_row, parent, false)
        return CheckoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckoutViewHolder, position: Int) {
        val cartItem = getItem(position)
        holder.bind(cartItem)
    }

    class CheckoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemNameTextView: TextView = itemView.findViewById(R.id.cartItemNameTextView)
        private val itemQuantityTextView: TextView = itemView.findViewById(R.id.cartItemQuantityTextView)
        private val itemPriceTextView: TextView = itemView.findViewById(R.id.cartItemPriceTextView)

        fun bind(cartItem: CartItem) {
            itemNameTextView.text = cartItem.item.name
            itemQuantityTextView.text = "Qty: ${cartItem.quantity}"
            itemPriceTextView.text = "$${cartItem.item.price * cartItem.quantity}"
        }
    }
}

class CheckoutItemDiffCallback : DiffUtil.ItemCallback<CartItem>() {
    override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
        return oldItem.item.id == newItem.item.id
    }

    override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
        return oldItem == newItem
    }
}
