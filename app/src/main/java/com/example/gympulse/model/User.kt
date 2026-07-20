package com.example.gympulse.model

import com.example.gympulse.util.Constants

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = Constants.ROLE_MEMBER,
    val gymId: String = "",
    val points: Int = 0,
    val streakCount: Int = 0
)
