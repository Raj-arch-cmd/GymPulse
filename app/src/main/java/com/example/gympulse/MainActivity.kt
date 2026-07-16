package com.example.gympulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gympulse.navigation.Routes
import com.example.gympulse.ui.theme.GymPulseTheme
import com.example.gympulse.ui.theme.screens.LoginScreen
import com.example.gympulse.ui.theme.screens.MemberHomeScreen
import com.example.gympulse.ui.theme.screens.OwnerHomeScreen
import com.example.gympulse.ui.theme.screens.RegisterScreen
import com.example.gympulse.ui.theme.screens.SelectGymScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GymPulseTheme {
                GymPulseApp()
            }
        }
    }
}

@Composable
fun GymPulseApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onMemberSuccess = {
                    navController.navigate(Routes.SELECT_GYM) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onOwnerSuccess = {
                    navController.navigate(Routes.OWNER_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onMemberSuccess = {
                    navController.navigate(Routes.SELECT_GYM) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onOwnerSuccess = {
                    navController.navigate(Routes.OWNER_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.SELECT_GYM) {
            SelectGymScreen(
                onGymSelected = { gym ->
                    navController.navigate(Routes.memberHome(gym.gymId)) {
                        popUpTo(Routes.SELECT_GYM) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.MEMBER_HOME) { backStackEntry ->
            val gymId = backStackEntry.arguments?.getString("gymId") ?: ""
            MemberHomeScreen(
                gymId = gymId,
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onChangeGym = {
                    navController.navigate(Routes.SELECT_GYM)
                }
            )
        }
        composable(Routes.OWNER_HOME) {
            OwnerHomeScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}