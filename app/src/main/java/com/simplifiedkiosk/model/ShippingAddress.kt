package com.simplifiedkiosk.model

data class ShippingAddress(
    val name: String,
    val addressLine1: String,
    val addressLine2: String,
    val city: String,
    val state: String,
    val zipCode: String
)
