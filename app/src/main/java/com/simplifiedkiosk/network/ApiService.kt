package com.simplifiedkiosk.network

import retrofit2.http.GET

interface ApiService {
    // Define API endpoints below
    @GET("/data/")
    suspend fun getData(): Any
}