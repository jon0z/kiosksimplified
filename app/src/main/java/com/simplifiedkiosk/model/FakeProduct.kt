package com.simplifiedkiosk.model

import com.google.gson.annotations.SerializedName
import java.io.Serial
import java.io.Serializable

data class FakeProduct(
    @SerializedName("id") val productId: Int,
    val title: String,
    val price: String,
    val category: String,
    val description: String,
    val imageUrl: String? = null,
): Serializable

fun FakeProduct.toFakeCartProduct() = FakeCartProduct(productId = productId, quantity = 1)
