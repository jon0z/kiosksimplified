package com.simplifiedkiosk.repository

import com.simplifiedkiosk.model.Product
import com.simplifiedkiosk.network.ProductsApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProductsRepository @Inject constructor(
    private val productsApiService: ProductsApiService
) {
    fun fetchProducts(): Flow<Result<List<Product>>> = flow {
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

    fun fetchSingleProduct(productId: Int): Flow<Result<Product>> = flow {
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

    fun searchProducts(query: String): Flow<Result<List<Product>>> = flow {
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