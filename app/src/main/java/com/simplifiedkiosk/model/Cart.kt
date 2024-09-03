package com.simplifiedkiosk.model

import com.simplifiedkiosk.dao.CartDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


private const val TAG = "Cart"
class Cart @Inject constructor(
    private val cartDao: CartDao
) {
    private val cartProducts = mutableListOf<ReactProduct>()

    suspend fun loadItemsFromDb(): Result<Map<String, String>> {
        var result: Result<Map<String, String>> = Result.failure(Throwable("cart is empty"))

        if (cartProducts.isEmpty()){
            // load from db and send
            val productsDb = cartDao.getAllCartItems().map { it.toReactProduct() }
            if(productsDb.isNotEmpty()){
                cartProducts.addAll(productsDb)
                result = updateCartMap()
            }
        } else {
            // send current loaded products
            result = updateCartMap()
        }
        return result
    }

    fun loadCartProducts(): Flow<List<Product>> = cartDao.getAllCartItemsFlow().map {
        it.map { it.toProduct() }
    }

    suspend fun addProduct(product: ReactProduct): Result<Map<String, String>> {
        val productIndex = cartProducts.indexOfFirst { it.productId == product.productId }
        if (productIndex != -1) {
            // product already in cart
            // update quantity
            cartProducts[productIndex].quantity = cartProducts[productIndex].quantity?.plus(1)
            // update product in database
            val rowId = cartDao.insertOrUpdateCartItem(cartProducts[productIndex].toCartItem())
            // send result
            return if (rowId != -1L) {
                updateCartMap()
            } else{
                Result.failure(Throwable("Failed to update product"))
            }
        } else {
            // product not in cart. Update quantity
            product.quantity = 1
            // add product to cart
            cartProducts.add(product)
            // add product to database
            val newRowId = cartDao.insertOrUpdateCartItem(product.toCartItem())
            // send result
            return if (newRowId != -1L) {
                updateCartMap()
            } else{
                Result.failure(Throwable("Failed to add product"))
            }
        }
    }

    suspend fun removeProduct(product: ReactProduct): Result<Map<String, String>> {
        var result: Result<Map<String, String>> = Result.failure(Throwable("Failed to remove product"))
        val productIndex = cartProducts.indexOfFirst { it.productId == product.productId }
        if (productIndex == -1) {
            return Result.failure(Throwable("Product not found in cart"))
        }

        val existingProduct = cartProducts[productIndex]
        existingProduct.quantity?.let {
            if(it == 1){
                val modifiedRow = cartDao.deleteCartItem(existingProduct.toCartItem())
                result = if(modifiedRow > 0){
                    if(productIndex < cartProducts.size) cartProducts.removeAt(productIndex)
                    updateCartMap()
                } else {
                    Result.failure(Throwable("Failed to remove product"))
                }
            } else {
                existingProduct.quantity = existingProduct.quantity?.minus(1)
                val modifiedRow = cartDao.insertOrUpdateCartItem(existingProduct.toCartItem())
                result = if(modifiedRow != -1L){
                    updateCartMap()
                } else {
                    Result.failure(Throwable("Failed to update product quantity"))
                }
            }
        }
        return result
    }

    // Get the list of items in the cart
    fun getProducts(): List<ReactProduct> {
        return cartProducts
    }

    // calculate cart total price
    fun getTotalPrice(): Double {
        var totalPrice = 0.0
        cartProducts.forEach {
            val price = it.price ?: 0.0
            val quantity = it.quantity ?: 0
            totalPrice += price * quantity
        }
        return totalPrice
    }


    // Clear all items from the cart
    fun clear() {
        cartProducts.clear()
    }

    fun getTotalQuantity(): Int {
        return cartProducts.sumOf { it.quantity ?: 0 }
    }

    fun containsProduct(productId: String): Boolean {
        return cartProducts.any { it.productId.toString() == productId }
    }

    private fun updateCartMap(): Result<Map<String, String>> {
        val cartMap = mutableMapOf<String, String>()
        cartMap["totalCartQuantity"] = getTotalQuantity().toString()
        cartMap["totalCartPrice"] = getTotalPrice().toString()
        return Result.success(cartMap.toMap())

    }
}
