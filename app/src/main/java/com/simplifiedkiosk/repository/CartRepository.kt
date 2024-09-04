package com.simplifiedkiosk.repository

import com.simplifiedkiosk.model.Cart
import com.simplifiedkiosk.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CartRepository @Inject constructor(
    private val cart: Cart
) {

    fun loadCartItems(): Flow<Result<Map<String, String>>> = flow {
        val result = cart.loadItemsFromDb()
        emit(result)
    }.catch { Result.failure<Map<String, String>>(it) }


    fun addProductToCart(product: Product): Flow<Result<Map<String, String>>> = flow {
        val result = cart.addProduct(product)
        emit(result)
    }.catch { Result.failure<Map<String, String>>(it) }

    fun removeProductFromCart(product: Product): Flow<Result<Map<String, String>>> = flow {
        val result = cart.removeProduct(product)
        emit(result)
    }.catch { Result.failure<Map<String, String>>(it) }

    fun getCartTotalPrice(): Double = cart.getTotalPrice()

    fun getCartTotalQuantity():Int = cart.getTotalQuantity()

    fun getCartItems(): List<Product> {
        return cart.getProducts()
    }

    fun clearCart(): Flow<Result<Boolean>> = flow {
        val result = if (cart.clear()) {
            Result.success(true)
        } else {
            Result.failure(Throwable("Failed to clear cart"))
        }
        emit(result)
    }
}
