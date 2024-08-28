package com.simplifiedkiosk.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object FakeProductsApiClient {
    private const val BASE_URL = "https://fakestoreapi.com/"
    private const val CONNECT_TIMEOUT = 15L // 15 seconds
    private const val READ_TIMEOUT = 30L // 30 seconds
    private const val WRITE_TIMEOUT = 30L // 30 seconds

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val apiService: FakeProductApiService by lazy {
        retrofit.create(FakeProductApiService::class.java)
    }
}