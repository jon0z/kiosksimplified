package com.simplifiedkiosk.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites_table")
data class FavoriteEntity(
    @PrimaryKey val productId: Int,
    val title: String,
    val price: Double,
    val description: String,
    val thumbnail: String,
    var isFavorite: Boolean = false
) {
    fun toReactProduct(): Product {
        return Product(
            productId = productId,
            title = title,
            price = price,
            description = description,
            thumbnail = thumbnail,
            isFavorite = isFavorite
        )
    }
}
