package com.simplifiedkiosk.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey (autoGenerate = true) val id: Long = 0,
    val itemId: String,
    val title: String?,
    val price: String?,
    val description: String?,
    val imgUrl: String?,
    val quantity: Int?,
){
    fun toProduct(): Product {
        return Product(
            productId = itemId.toInt(),
            title = title,
            price = price,
            description = description,
            imageUrl = imgUrl,
            quantity = quantity,
            dbId = id
        )
    }
}
