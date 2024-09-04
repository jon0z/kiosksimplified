package com.simplifiedkiosk.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Product(
    @SerializedName("id")
    val productId: Int? = null,
    val title: String? = null,
    val price: Double? = null,
    val description: String? = null,
    val category: String? = null,
    val rating: Double? = null,
    var quantity: Int? = null,
    val thumbnail: String? = null,
    val images: List<String>? = null,
    var isFavorite: Boolean? = false
): Serializable {
    fun toCartItem(): CartItemEntity {
        return CartItemEntity(
            itemId = productId.toString(),
            title = title,
            price = price.toString(),
            description = description,
            imgUrl = thumbnail,
            quantity = quantity,
            isFavorite = isFavorite
        )
    }

    fun toFavoriteEntity(): FavoriteEntity {
        return FavoriteEntity(
            productId = productId ?: 0,
            title = title ?: "",
            price = price ?: 0.0,
            description = description ?: "",
            thumbnail = thumbnail ?: "",
            isFavorite = isFavorite ?: false
        )
    }
}

