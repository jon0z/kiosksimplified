package com.simplifiedkiosk.repository

import androidx.room.util.query
import com.simplifiedkiosk.model.ReactProduct
import com.simplifiedkiosk.network.ReacProductsApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ReactProductsRepository @Inject constructor(
    private val productsApiService: ReacProductsApiService
) {
    fun fetchReactProducts(): Flow<Result<List<ReactProduct>>> = flow {
        val response = productsApiService.fetchProducts()
        if (response.isSuccessful) {
            response.body()?.let { productsResponse ->
                val products = productsResponse.products
                emit(Result.success(products))
            } ?: emit(Result.failure(Throwable("No products found")))
        } else {
            response.errorBody()?.let { error ->
                emit(Result.failure(Exception(error.string())))
            } ?: emit(Result.failure(Throwable("Failed to fetch products from server code: ${response.code()}")))
        }
    }

    fun fetchReactProduct(productId: Int): Flow<Result<ReactProduct>> = flow {
        val response = productsApiService.fetchReactProductById(productId)
        if (response.isSuccessful) {
            response.body()?.let { product ->
                emit(Result.success(product))
            } ?: emit(Result.failure(Throwable("No product found")))
        } else {
            response.errorBody()?.let { error ->
                emit(Result.failure(Exception(error.string())))
            } ?: emit(Result.failure(Throwable("Failed to fetch product from server code: ${response.code()}")))
        }
    }

    fun searchProducts(query: String): Flow<Result<List<ReactProduct>>> = flow {
        val response = productsApiService.searchProducts(query)
        if (response.isSuccessful) {
            response.body()?.let { productsResponse ->
                val products = productsResponse.products
                emit(Result.success(products))
            } ?: emit(Result.failure(Throwable("No products found")))
        } else {
            response.errorBody()?.let { error ->
                emit(Result.failure(Exception(error.string())))
            } ?: emit(Result.failure(Throwable("Failed to fetch products from server code: ${response.code()}")))
        }
    }
}