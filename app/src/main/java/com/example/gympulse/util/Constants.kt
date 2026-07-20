package com.example.gympulse.util

object Constants {
    // User Roles
    const val ROLE_OWNER = "owner"
    const val ROLE_MEMBER = "member"

    // Session Statuses
    const val SESSION_STATUS_ACTIVE = "active"
    const val SESSION_STATUS_COMPLETED = "completed"

    // Checkout Types
    const val CHECKOUT_TYPE_MANUAL = "MANUAL"
    const val CHECKOUT_TYPE_AUTO = "AUTO"

    // Firestore Fields (Optional but good for consistency)
    const val FIELD_ROLE = "role"
    const val FIELD_SESSION_STATUS = "sessionStatus"
    const val FIELD_GYM_ID = "gymId"
    const val FIELD_USER_ID = "userId"
    const val FIELD_CURRENT_COUNT = "currentCount"

    // Recovery Mechanism
    const val REMINDER_DELAY_MINUTES = 10L
    const val AUTO_CHECKOUT_DELAY_MINUTES = 5L
    const val CHANNEL_ID_RECOVERY = "gympulse_recovery"
    const val CHANNEL_NAME_RECOVERY = "Session Recovery"
    const val NOTIFICATION_ID_REMINDER = 2001
    const val NOTIFICATION_ID_AUTO_CHECKOUT = 2002
}
