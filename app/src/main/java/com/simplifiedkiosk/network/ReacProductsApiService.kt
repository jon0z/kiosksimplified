package com.simplifiedkiosk.network

import com.simplifiedkiosk.model.ProductsResponse
import com.simplifiedkiosk.model.Product
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ReacProductsApiService {

    @GET("/products")
    suspend fun fetchProducts(): Response<ProductsResponse>

    @GET("/products/{productId}")
    suspend fun fetchReactProductById(
        @Path("productId") productId: Int
    ): Response<Product>

    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String
    ): Response<ProductsResponse>
}