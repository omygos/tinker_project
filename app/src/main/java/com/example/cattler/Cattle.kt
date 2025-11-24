package com.example.cattler
// File: Cattle.kt
// This is the blueprint for a single cow's data on the dashboard.
// The variable names (id, temperature, distance) MUST EXACTLY MATCH
// the keys in the JSON response from your Raspberry Pi.


data class Cattle(
    val id: String,
    val temperature: Double,    // ✅ Must be Double
    val distance: Double,       // ✅ Must be Double (for 0.43)
    val last_updated: String?   // ✅ Must be added (your server sends this)
)