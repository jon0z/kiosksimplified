package com.simplifiedkiosk.model

data class CartItem(
    val item: Item,
    var quantity: Int = 1
)
