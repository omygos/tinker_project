package com.example.cattler

// File: CattleHistory.kt
// This is the blueprint for a single data point in the history graph.


data class DataPoint(
    val timestamp: String,
    val temperature: Double,
    val distance: Int
)