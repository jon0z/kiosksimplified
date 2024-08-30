package com.simplifiedkiosk.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.simplifiedkiosk.R
import com.simplifiedkiosk.model.Product

class CartAdapter(private val onRemoveItemClick: (Product) -> Unit, private val onAddItemClick: (Product) -> Unit) : ListAdapter<Product, CartAdapter.CartViewHolder>(CartItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_product_row_layout, parent, false)
        return CartViewHolder(view, onRemoveItemClick, onAddItemClick, this)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = getItem(position)
        holder.bind(cartItem)
    }

    class CartViewHolder(itemView: View,
                         private val onRemoveItemClick: (Product) -> Unit,
                         private val onAddItemClick: (Product) -> Unit,
                         private val cartAdapter: CartAdapter,
    ) : RecyclerView.ViewHolder(itemView) {
        private val itemNameTextView: TextView = itemView.findViewById(R.id.cart_item_name_textview)
        private val itemQuantityTextView: TextView = itemView.findViewById(R.id.cart_item_quantity)
        private val itemPriceTextView: TextView = itemView.findViewById(R.id.cart_item_price_textview)
        private val itemDescriptionTextView: TextView = itemView.findViewById(R.id.cartItemDescriptionTextView)
        private val itemRemoveButton: ImageButton = itemView.findViewById(R.id.decrease_quantity_btn)
        private val itemAddButton: ImageButton = itemView.findViewById(R.id.increase_quantity_btn)

        fun bind(cartProduct: Product) {
            itemNameTextView.text = cartProduct.title
            itemDescriptionTextView.text = cartProduct.description
            itemQuantityTextView.text = "Qty: ${cartProduct.quantity}"
            if(cartProduct.price != null && cartProduct.quantity != null){
                cartProduct.quantity?.let {
                    val totalPrice = cartProduct.price.toDouble() * it
                    itemPriceTextView.text = "$$totalPrice"
                }
            } else {
                itemPriceTextView.text = "$0.00"
            }
            itemRemoveButton.setOnClickListener {
                cartProduct.productId?.let {
                    cartAdapter.removeItem(it.toString())
                }
                onRemoveItemClick.invoke(cartProduct)
            }

            itemAddButton.setOnClickListener {
                cartAdapter.updateCartItem(cartProduct)
                onAddItemClick.invoke(cartProduct)
            }
        }
    }

    fun updateCartItem(newProduct: Product){
        val existingItemPosition = currentList.indexOfFirst { it.productId == newProduct.productId }
        if (existingItemPosition != -1){
            val existingItem = getItem(existingItemPosition)
            if(existingItem.quantity!! > 1){
                existingItem.quantity = existingItem.quantity!! - 1
                notifyItemChanged(existingItemPosition)
            } else {
                removeItemInternally(existingItemPosition)
            }
        } else {
            submitList(currentList + newProduct)
        }
    }

    fun removeItem(productId: String){
        if (productId.isNotBlank()){
            val existingItemPosition = currentList.indexOfFirst { it.productId == productId.toInt() }
            if (existingItemPosition != -1){
                removeItemInternally(existingItemPosition)
            }
        }
    }

    private fun removeItemInternally(position: Int){
        currentList.removeAt(position)
        notifyItemRemoved(position)
        if(currentList.isEmpty()){
            submitList(emptyList())
        } else {
            notifyItemRangeChanged(position, currentList.size)
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
