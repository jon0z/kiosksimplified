package com.simplifiedkiosk.model

import java.io.Serializable

data class FakeCartProduct(
    val productId: Int,
    var quantity: Int? = null
): Serializable
