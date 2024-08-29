package com.simplifiedkiosk.model.commercejs

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AddItemRequestBodyParams(
    @SerializedName("id") val productId: String,
    val quantity: Int?
): Serializable
