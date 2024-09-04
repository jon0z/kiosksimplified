package com.simplifiedkiosk.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val itemId: String,
    val title: String?,
    val price: String?,
    val description: String?,
    val imgUrl: String?,
    val quantity: Int?,
    var isFavorite: Boolean? = false
){
    fun toReactProduct(): Product {
        return Product(
            productId = itemId.toInt(),
            title = title,
            price = price?.toDouble(),
            description = description,
            quantity = quantity,
            thumbnail = imgUrl
        )
    }
}
