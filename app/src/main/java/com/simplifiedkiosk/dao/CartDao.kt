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
    @Query("SELECT * FROM cart_items")
    suspend fun getAllCartItems(): List<CartItemEntity>

    @Query("SELECT * FROM cart_items")
    fun getAllCartItemsFlow(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItemEntity): Long // returns rowId for newly inserted item

    @Delete
    suspend fun deleteCartItem(cartItem: CartItemEntity): Int // returns number of rows deleted

    @Update
    suspend fun updateCartItem(cartItem: CartItemEntity): Int // returns number of rows updated

    @Query("SELECT * FROM cart_items WHERE itemId = :itemId")
    suspend fun getCartProductByItemId(itemId: String): List<CartItemEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM cart_items WHERE id = :id)")
    suspend fun cartItemExists(id: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM cart_items WHERE itemId = :itemId)")
    suspend fun cartProductWithItemIdExists(itemId: String): Boolean
}
