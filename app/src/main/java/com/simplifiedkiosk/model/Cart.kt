package com.simplifiedkiosk.model

import com.simplifiedkiosk.dao.CartDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


private const val TAG = "Cart"
class Cart @Inject constructor(
    private val cartDao: CartDao
) {
    private val products = mutableListOf<ReactProduct>()

    suspend fun loadItemsFromDb(): Result<Map<String, String>> {
        var result: Result<Map<String, String>> = Result.failure(Throwable("Failed to add to cart"))

        if (products.isEmpty()){
            // load from db and send
            val productsDb = cartDao.getAllCartItems().map { it.toReactProduct() }
            if(productsDb.isNotEmpty()){
                products.addAll(productsDb)
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
        val productWithDbId = if(product.dbId == null){
            product.toCartItem().toReactProduct()
        } else product

        val isIteminDb = cartDao.cartProductWithItemIdExists(productWithDbId.productId.toString())

        if (!isIteminDb ) {
            productWithDbId.quantity = 1
            val newRowId = cartDao.insertCartItem(productWithDbId.toCartItem())
            return if(newRowId != -1L) {
                products.add(productWithDbId)
                updateCartMap()
            } else {
                Result.failure(Throwable("Failed to add to cart"))
            }
        } else {
            val productsFromDb = cartDao.getCartProductByItemId(productWithDbId.productId.toString())
            val existingDbProduct = productsFromDb.first().toProduct()
            existingDbProduct.quantity = (existingDbProduct.quantity ?: 0) + 1
            val rowUpdated = cartDao.updateCartItem(existingDbProduct.toCartItem())

            return if(rowUpdated != 0) {
                products.add(productWithDbId)
                updateCartMap()
            } else {
                Result.failure(Throwable("Failed to add to cart"))
            }
        }
    }

    suspend fun removeProduct(product: ReactProduct): Result<Map<String, String>> {
        val productWithDbId: ReactProduct = if (product.dbId == null) product.toCartItem().toReactProduct() else product
        val isItemInDb = cartDao.cartProductWithItemIdExists(productWithDbId.productId.toString())

        if (isItemInDb) {
            val productsFromDb = cartDao.getCartProductByItemId(productWithDbId.productId.toString())
            val existingDbProduct = productsFromDb.first().toProduct()
            val rowModified = existingDbProduct.quantity?.let {
                if(it > 1) {
                    existingDbProduct.quantity = it - 1
                    cartDao.updateCartItem(existingDbProduct.toCartItem())
                } else {
                    existingDbProduct.quantity = 0
                    cartDao.deleteCartItem(existingDbProduct.toCartItem())
                }
            }
            return if(rowModified != 0){
                products.remove(productWithDbId)
                updateCartMap()
            } else {
                Result.failure(Throwable("Failed to internally remove product from cart"))
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
    fun getProducts(): List<ReactProduct> {
        return products
    }

    // calculate cart total price
    fun getTotalPrice(): Double {
        var totalPrice = 0.0
        products.forEach {
            val price = it.price ?: 0.0
            val quantity = it.quantity ?: 0
            totalPrice += price * quantity
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
        cartMap["totalCartIte"]
        return Result.success(cartMap.toMap())

    }
}
