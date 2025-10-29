package com.example.cattler

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // The 'api' property should be of type ApiService
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.1.10:5000/") // Make sure this IP is correct
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}