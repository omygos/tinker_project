package com.example.cattler

import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("api/cattle")
    suspend fun getAllCattle(): List<Cattle>

    // Corrected: This now expects the server to return a direct JSON array of data points.
    @GET("api/cattle/{cowId}")
    suspend fun getCattleHistory(@Path("cowId") id: String): List<DataPoint>
}