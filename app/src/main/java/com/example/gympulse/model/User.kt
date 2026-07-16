package com.example.gympulse.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "member",
    val gymId: String = "",
    val points: Int = 0,
    val streakCount: Int = 0
)