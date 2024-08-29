package com.simplifiedkiosk.network

import com.simplifiedkiosk.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object CommerceJsApiClient {
    private const val BASE_URL = "https://api.chec.io/v1/"
    private const val SANDBOX_API_KEY = BuildConfig.COMMERCEJS_SANDBOX_API_KEY
    private const val PRODUCTION_API_KEY = BuildConfig.COMMERCEJS_LIVE_API_KEY
    private const val CONNECT_TIMEOUT = 15L // 15 seconds
    private const val READ_TIMEOUT = 30L // 30 seconds
    private const val WRITE_TIMEOUT = 30L // 30 seconds


    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-Authorization", SANDBOX_API_KEY)
                    .build()
                chain.proceed(request)
            }
            .build()
    }
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: CommerceJsApiService by lazy {
        retrofit.create(CommerceJsApiService::class.java)
    }
}