package com.simplifiedkiosk.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class FakeCart(
    @SerializedName("id") val cartId: String,
    val userId: String,
    val date: String,
    val products: List<FakeCartProduct>
):Serializable
