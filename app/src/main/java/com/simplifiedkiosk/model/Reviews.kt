package com.simplifiedkiosk.model

data class Reviews(
    val rating: Int,
    val comment: String,
    val date: String, // "2022-04-26T00:00:00.000Z"
    val reviewerName: String,
    val reviewerEmail: String,
)
