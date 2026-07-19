package com.example.gympulse.navigation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MEMBER_HOME = "member_home/{gymId}"
    const val OWNER_HOME = "owner_home"
    const val SELECT_GYM = "select_gym"

    fun memberHome(gymId: String) = "member_home/$gymId"
}