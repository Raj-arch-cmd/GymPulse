package com.example.gympulse.model

import com.google.firebase.Timestamp

data class Session(
    val sessionId: String = "",
    val userId: String = "",
    val gymId: String = "",
    val checkInTime: Timestamp? = null,
    val checkOutTime: Timestamp? = null,
    val dayOfWeek: String = "",
    val hourSlot: Int = 0,
    val sessionStatus: String = "active",
    val autoCheckedOut: Boolean = false
)