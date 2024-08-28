package com.simplifiedkiosk.repository

import android.util.Log
import com.simplifiedkiosk.model.FakeCart
import com.simplifiedkiosk.model.FakeCartProduct
import com.simplifiedkiosk.model.FakeProduct
import com.simplifiedkiosk.model.Item
import com.simplifiedkiosk.network.FakeProductApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import javax.inject.Inject


private const val TAG = "ProductsRepository"
class ProductsRepository @Inject constructor(
    private val apiService: FakeProductApiService
) {
    fun fetchProducts(): Flow<Result<List<FakeProduct>>>  = flow {
        val response = apiService.fetchProducts()
        if (response.isSuccessful) {
            response.body()?.let { products ->
                emit(Result.success(products))
            } ?: emit(Result.failure(Throwable("No products found")))
        } else {
            emit(Result.failure(Throwable("Failed to fetch products")))
        }
    }.retry (2)
        .catch { emit(Result.failure(it)) }

    fun fetchProductById(productId: String): Flow<Result<FakeProduct>> = flow {
        val response = apiService.fetchProductById(productId)
        if (response.isSuccessful) {
            response.body()?.let { product ->
                emit(Result.success(product))
            } ?: emit(Result.failure(Throwable("No product found")))
        } else {
            emit(Result.failure(Throwable("Failed to fetch product")))
        }
    }.retry (2)
        .catch { emit(Result.failure(it)) }

    fun addToCart(userId: String,
                  date: String, // format: yyyy-MM-dd
                  products: List<FakeCartProduct>
    ): Flow<Result<FakeCart>> = flow {
        val response = apiService.addToCart(userId, date, products)
        if (response.isSuccessful) {
            response.body()?.let { cart ->
                Log.e(TAG, "success cart created: $cart")
                emit(Result.success(cart))
            } ?: emit(Result.failure(Throwable("No cart found")))
        } else {
            Log.e(TAG, "Failed to create cart")
            emit(Result.failure(Throwable("Failed to add to cart")))
        }
    }.retry(2)
        .catch { emit(Result.failure(it)) }

    fun getCartByUserId(userId: String): Flow<Result<List<FakeCart>>> = flow {
        val response = apiService.fetchCartByUserId(userId)
        if (response.isSuccessful) {
            response.body()?.let { carts ->
                emit(Result.success(carts))
            } ?: emit(Result.failure(Throwable("No carts found for user $userId")))
        } else {
            emit(Result.failure(Throwable("Failed to fetch carts")))
        }
    }.retry (2)
        .catch { emit(Result.failure(it)) }
}
