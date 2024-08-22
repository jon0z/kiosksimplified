package com.simplifiedkiosk.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey (autoGenerate = true) val id: Long = 0,
    val itemId: String,
    val name: String,
    val description: String,
    val price: Double,
    val quantity: Int,
    val imgUrl: String
) {
    // Convert to a CartItem model
    fun toCartItem(): CartItem {
        return CartItem(
            item = Item(itemId, name, description, price, imgUrl),
            quantity = quantity
        )
    }
}
