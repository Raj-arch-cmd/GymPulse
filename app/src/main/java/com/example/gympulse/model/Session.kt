package com.example.gympulse.model

import com.example.gympulse.util.Constants
import com.google.firebase.Timestamp

data class Session(
    val sessionId: String = "",
    val userId: String = "",
    val gymId: String = "",
    val checkInTime: Timestamp? = null,
    val checkOutTime: Timestamp? = null,
    val duration: Long = 0, // Duration in minutes
    val dayOfWeek: String = "",
    val hourSlot: Int = 0,
    val sessionStatus: String = Constants.SESSION_STATUS_ACTIVE,
    val checkoutType: String = "" // "MANUAL" or "AUTO"
)
