package com.simplifiedkiosk.model.commercejs

import com.google.gson.annotations.SerializedName

data class UpdateCartBodyParams(
    @SerializedName("discount_code") val discountCode: String,
    val meta: Any?
)
