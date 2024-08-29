package com.simplifiedkiosk.model

import java.io.Serializable

data class FakeCartProduct(
    val productId: Int,
    var quantity: Int? = null
): Serializable {
    fun toFakeProduct(): Product {
        return Product(
            productId = productId,
            title = null,
            price = null,
            description = null,
            quantity = quantity)
    }
}
