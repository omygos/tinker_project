package com.example.cattler

import retrofit2.http.Body // <-- ADD THIS IMPORT
import retrofit2.http.GET
import retrofit2.http.POST // <-- ADD THIS IMPORT
import retrofit2.http.Path

// This data class is for sending the token
data class FcmTokenRequest(val token: String)

interface ApiService {

    @GET("api/cattle")
    suspend fun getAllCattle(): List<Cattle>

    @GET("api/cattle/{cowId}")
    suspend fun getCattleHistory(@Path("cowId") id: String): List<DataPoint>

    // ✅ ADD THIS NEW FUNCTION
    @POST("api/register-token")
    suspend fun registerDeviceToken(@Body tokenRequest: FcmTokenRequest)
}