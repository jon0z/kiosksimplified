package com.simplifiedkiosk.ui.cart

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.simplifiedkiosk.R
import com.simplifiedkiosk.model.Product
import com.squareup.picasso.Picasso

private const val TAG = "CartAdapter"
class CartAdapter(private val onRemoveItemClick: (Product) -> Unit, private val onAddItemClick: (Product) -> Unit) : ListAdapter<Product, CartAdapter.CartViewHolder>(CartItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_product_row_layout, parent, false)
        return CartViewHolder(view, onRemoveItemClick, onAddItemClick)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = getItem(position)
        holder.bind(cartItem)
    }

    class CartViewHolder(
        itemView: View,
        private val onRemoveItemClick: (Product) -> Unit,
        private val onAddItemClick: (Product) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {
        private val itemNameTextView: TextView = itemView.findViewById(R.id.cart_item_name_textview)
        private val itemQuantityTextView: TextView = itemView.findViewById(R.id.cart_item_quantity)
        private val itemPriceTextView: TextView = itemView.findViewById(R.id.cart_item_price_textview)
        private val itemDescriptionTextView: TextView = itemView.findViewById(R.id.cartItemDescriptionTextView)
        private val itemRemoveButton: ImageButton = itemView.findViewById(R.id.decrease_quantity_btn)
        private val itemAddButton: ImageButton = itemView.findViewById(R.id.increase_quantity_btn)
        private val itemImage: ImageView = itemView.findViewById(R.id.productImage)

        fun bind(cartProduct: Product) {
            itemNameTextView.text = cartProduct.title
            itemDescriptionTextView.text = cartProduct.description
            itemQuantityTextView.text = "Qty: ${cartProduct.quantity}"
            if(cartProduct.thumbnail != null){
                Picasso.get().load(cartProduct.thumbnail).into(itemImage)
            } else {
                itemImage.setImageResource(R.mipmap.product_image_placeholder_48x48)
            }

            if(cartProduct.price != null && cartProduct.quantity != null){
                cartProduct.quantity?.let {
                    val totalPrice = cartProduct.price.toDouble() * it
                    itemPriceTextView.text = "$$totalPrice"
                }
            } else {
                itemPriceTextView.text = "$0.00"
            }
            itemRemoveButton.setOnClickListener {
                onRemoveItemClick.invoke(cartProduct)
            }

            itemAddButton.setOnClickListener {
                onAddItemClick.invoke(cartProduct)
            }
        }
    }

    fun updateCartItem(newProduct: Product){
        val existingItemPosition = currentList.indexOfFirst { it.productId == newProduct.productId }
        if (existingItemPosition != -1){
            val existingItem = getItem(existingItemPosition)
            existingItem.quantity?.let {
                if(it >= 1){
                    existingItem.quantity = it + 1
                    notifyItemChanged(existingItemPosition)
                }
            }
        } else {
            // if currentList is empty add product
            val newList = currentList + newProduct
            submitList(newList)
        }
    }

    fun removeItem(productId: String){
        if (productId.isNotBlank()){
            val existingItemPosition = currentList.indexOfFirst { it.productId == productId.toInt() }
            if (existingItemPosition != -1){
                val existingItem = currentList.getOrNull(existingItemPosition)
                if(existingItem != null){
                    existingItem.quantity?.let {
                        if(it > 1){
                            val updatedItem = existingItem.copy(quantity = it - 1)
                            val newList = currentList.toSet().minus(existingItem).plus(updatedItem).toList()
                            submitList(newList)
                        } else {
                            val newList = currentList.toSet().minus(existingItem).toList()
                            submitList(newList)
                        }

                    }
                }
            } else {
                Log.d(TAG, "removeItem: item not found... returned index $existingItemPosition")
            }
        }
    }
}

class CartItemDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.quantity == newItem.quantity
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}
