package com.simplifiedkiosk.model

import android.util.Log
import com.simplifiedkiosk.dao.CartDao
import javax.inject.Inject


private const val TAG = "Cart"
class Cart @Inject constructor(
    private val cartDao: CartDao
) {
    private var products = mutableListOf<Product>()

    suspend fun loadItemsFromDb(): Result<Map<String, String>> {
        var result: Result<Map<String, String>> = Result.failure(Throwable("Failed to add to cart"))
        val productsDb = cartDao.getAllCartItems().map { it.toProduct() }
        if(productsDb.isNotEmpty()){
            products.clear()
            products.addAll(productsDb)
            val cartMap = mutableMapOf<String, String>()
            cartMap["totalCartQuantity"] = getTotalQuantity().toString()
            cartMap["totalCartPrice"] = getTotalPrice().toString()
            result = Result.success(cartMap.toMap())
        }
        return result
    }

    // Add an item to the cart
    suspend fun addProduct(product: Product): Result<Map<String, String>> {
        Log.e(TAG, "*** addProduct: got button click", )
        val productWithDbId = if(product.dbId == null){
            product.toCartItem().toProduct()
        } else product

        val isProductInDb = if(productWithDbId.dbId == null) {
            false
        } else {
            cartDao.cartItemExists(productWithDbId.dbId)
        }
        Log.e(TAG, "*** isProductInDb: $isProductInDb", )

        if (!isProductInDb ) {
            Log.e(TAG, "*** product is in db", )
            productWithDbId.quantity = 1
            val newRowId = cartDao.insertCartItem(productWithDbId.toCartItem())
            return if(newRowId != -1L) {
                products.add(productWithDbId)
                updateCartMap()
            } else {
                Result.failure(Throwable("Failed to add to cart"))
            }
        } else {
            Log.e(TAG, "*** product is not in db", )
            productWithDbId.quantity = (productWithDbId.quantity ?: 0) + 1
            val rowUpdated = cartDao.updateCartItem(productWithDbId.toCartItem())
            return if(rowUpdated != 0) {
                products.add(productWithDbId)
                updateCartMap()
            } else {
                Result.failure(Throwable("Failed to add to cart"))
            }
        }
    }

    // Remove an item from the cart
    suspend fun removeProduct(product: Product): Result<Map<String, String>> {
        val isProductInDb = if(product.dbId != null) cartDao.cartItemExists(product.dbId) else false
        if (isProductInDb) {
            val rowDeleted = product.dbId?.let { cartDao.deleteCartItem(product.toCartItem()) }  ?: -1
            return if(rowDeleted != 0){
                products.remove(product)
                updateCartMap()
            } else {
                Result.failure(Throwable("Failed to remove product from cart"))
            }
        } else {
            return if(getTotalQuantity() > 0) {
                Result.failure(Throwable("Product not found in cart"))
            } else {
                Result.failure(Throwable("Cart is empty"))
            }
        }
    }

    // Get the list of items in the cart
    fun getProducts(): List<Product> {
        return products
    }

    // calculate cart total price
    fun getTotalPrice(): Double {
        var totalPrice = 0.0
        products.forEach {
            val price = it.price?.toDouble() ?: 0.0
            val quantity = it.quantity ?: 0
            totalPrice = price * quantity
        }

        return totalPrice
    }


    // Clear all items from the cart
    fun clear() {
        products.clear()
    }

    fun getTotalQuantity(): Int {
        return products.sumOf { it.quantity ?: 0 }
    }

    fun containsProduct(productId: String): Boolean {
        return products.any { it.productId.toString() == productId }
    }

    private fun updateCartMap(): Result<Map<String, String>> {
        val cartMap = mutableMapOf<String, String>()
        cartMap["totalCartQuantity"] = getTotalQuantity().toString()
        cartMap["totalCartPrice"] = getTotalPrice().toString()
        return Result.success(cartMap.toMap())

    }
}
