package com.simplifiedkiosk.repository

import com.simplifiedkiosk.model.Product
import com.simplifiedkiosk.network.FakeProductApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

private const val TAG = "ProductsRepository"
class ProductsRepository @Inject constructor(
    private val apiService: FakeProductApiService
) {
    fun fetchProducts(): Flow<Result<List<Product>>>  = flow {
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
}
