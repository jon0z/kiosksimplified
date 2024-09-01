package com.simplifiedkiosk.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReactProduct (
    @SerializedName("id") val productId: Int? = null,
    val title: String? = null,
    val price: Double? = null,
    val description: String? = null,
    val category: String? = null,
    val rating: Double? = null,
    var quantity: Int? = null,
    var reviews: List<Reviews>? = null,
    val thumbnail: String? = null,
    val images: List<String>? = null,
    val dbId: Long? = null
): Serializable {
    fun toCartItem(): CartItemEntity {
        return CartItemEntity(
            id = dbId ?: 0,
            itemId = productId.toString(),
            title = title,
            price = price.toString(),
            description = description,
            imgUrl = thumbnail,
            quantity = quantity
        )
    }
}

