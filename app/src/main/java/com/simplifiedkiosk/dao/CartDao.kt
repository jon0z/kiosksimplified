package com.simplifiedkiosk.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.simplifiedkiosk.model.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCartItem(cartItem: CartItemEntity): Long

    @Query("DELETE FROM cart_items")
    suspend fun deleteAllCartItems()

    @Query("SELECT * FROM cart_items")
    suspend fun getAllCartItems(): List<CartItemEntity>

    @Delete
    suspend fun deleteCartItem(cartItem: CartItemEntity): Int // returns number of rows deleted
}
