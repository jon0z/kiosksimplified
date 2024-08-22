package com.simplifiedkiosk.repository

import com.simplifiedkiosk.dao.CartItemDao
import com.simplifiedkiosk.model.Cart
import com.simplifiedkiosk.model.CartItem
import com.simplifiedkiosk.model.CartItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CartRepository @Inject constructor(
    private val cartItemDao: CartItemDao,
    private val cart: Cart
) {

    suspend fun getAllCartItems(): List<CartItem> {
        if(cart.getItems().isEmpty()){
            val cartItemsFromDb = cartItemDao.getAllCartItems().map { it.toCartItem() }
            cartItemsFromDb.forEach { cart.addItem(it.item) }
        }
        return cart.getItems()
    }

    suspend fun addItemToCart(cartItem: CartItem) {
        withContext(Dispatchers.IO){
            val existingItemId = cartItemDao.insertCartItem(
                CartItemEntity(
                    itemId = cartItem.item.id,
                    name = cartItem.item.name,
                    description = cartItem.item.description,
                    price = cartItem.item.price,
                    quantity = cartItem.quantity,
                    imgUrl = cartItem.item.imageUrl
                )
            )
            if(existingItemId == -1L){
                cartItemDao.incrementQuantity(cartItem.item.id, cartItem.quantity)
            }
        }
        cart.addItem(cartItem.item)
    }

    suspend fun clearCart() {
        cart.clear()
        withContext(Dispatchers.IO){
            cartItemDao.clearCart()
        }
    }

    suspend fun removeItemFromCart(itemId: String) {
        cart.removeItem(itemId)
        withContext(Dispatchers.IO){
            cartItemDao.deleteCartItem(itemId)
        }
    }
}
