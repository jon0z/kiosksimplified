package com.simplifiedkiosk.network

import com.simplifiedkiosk.model.FakeCart
import com.simplifiedkiosk.model.FakeCartProduct
import com.simplifiedkiosk.model.FakeProduct
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FakeProductApiService {
    // Define API endpoints below
    @GET("products")
    suspend fun fetchProducts(): Response<List<FakeProduct>>

    @GET("products/{productId}")
    suspend fun fetchProductById(@Path("productId") productId: String? = "1"): Response<FakeProduct>

    @POST("carts")
    suspend fun addToCart(
        @Query("userId") userId: String,
        @Query("date") date: String,
        @Query("products") products: List<FakeCartProduct>
    ): Response<FakeCart>

    @GET("carts/user/{userId}")
    suspend fun fetchCartByUserId(
        @Path("userId") userId: String
    ): Response<List<FakeCart>>
}