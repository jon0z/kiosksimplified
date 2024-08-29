package com.simplifiedkiosk.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Product(
    @SerializedName("id") val productId: Int? = null,
    val title: String? = null,
    val price: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    var quantity: Int? = null,
    val dbId: Long? = null
): Serializable {

    fun toCartItem(): CartItemEntity {
        return CartItemEntity(
            id = dbId ?: 0,
            itemId = productId.toString(),
            title = title,
            price = price,
            description = description,
            imgUrl = imageUrl,
            quantity = quantity
        )
    }
}