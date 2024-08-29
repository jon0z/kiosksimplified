package com.simplifiedkiosk.network

import com.simplifiedkiosk.model.commercejs.AddItemRequestBodyParams
import com.simplifiedkiosk.model.commercejs.UpdateCartBodyParams
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CommerceJsApiService {

    @GET("v1/carts")
    suspend fun createCart()

    @GET("v1/carts/{cartId}")
    suspend fun getCartByCartId(
        @Path("cartId") cart_id: String
    )

    @POST("v1/carts/{cartId}")
    suspend fun addItemToCart(
        @Path("cartId") cart_id: String,
        @Body bodyParams: AddItemRequestBodyParams
    )

    @PUT("v1/carts/{cart_id}")
    suspend fun updateCart(
        @Path("cart_id") cart_id: String,
        @Body bodyParams: UpdateCartBodyParams
    )

    @DELETE("v1/carts/{cart_id}")
    suspend fun deleteCart(
        @Path("cart_id") cart_id: String
    )

    @DELETE("v1/carts/{cart_id}/items")
    suspend fun emptyCart(
        @Path("cart_id") cart_id: String
    )

    @DELETE("v1/carts/{cart_id}/items/{line_item_id}")
    suspend fun deleteCartItem(
        @Path("cart_id") cart_id: String,
        @Path("line_item_id") line_item_id: String
    )

}