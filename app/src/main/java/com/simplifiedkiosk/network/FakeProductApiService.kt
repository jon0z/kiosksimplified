package com.simplifiedkiosk.network

import com.simplifiedkiosk.model.Product
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface FakeProductApiService {
    // Define API endpoints below
    @GET("products")
    suspend fun fetchProducts(): Response<List<Product>>

    @GET("products/{productId}")
    suspend fun fetchProductById(@Path("productId") productId: String? = "1"): Response<Product>
}