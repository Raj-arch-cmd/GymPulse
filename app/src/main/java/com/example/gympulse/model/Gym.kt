package com.example.gympulse.model

data class Gym(
    val gymId: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val currentCount: Int = 0,
    val ownerId: String = ""
)