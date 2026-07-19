package com.example.gympulse.ui.theme.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gympulse.navigation.Routes
import com.example.gympulse.viewmodel.AuthViewModel

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToOwnerHome: () -> Unit,
    onNavigateToSelectGym: () -> Unit,
    onNavigateToMemberHome: (String) -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val splashState by authViewModel.splashState.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.checkCurrentUser()
    }

    LaunchedEffect(splashState) {
        when (splashState) {
            is com.example.gympulse.viewmodel.SplashState.NavigateToLogin -> {
                onNavigateToLogin()
            }
            is com.example.gympulse.viewmodel.SplashState.NavigateToOwnerHome -> {
                onNavigateToOwnerHome()
            }
            is com.example.gympulse.viewmodel.SplashState.NavigateToSelectGym -> {
                onNavigateToSelectGym()
            }
            is com.example.gympulse.viewmodel.SplashState.NavigateToMemberHome -> {
                val user = (splashState as com.example.gympulse.viewmodel.SplashState.NavigateToMemberHome).user
                onNavigateToMemberHome(user.gymId)
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (splashState) {
            is com.example.gympulse.viewmodel.SplashState.Loading -> {
                CircularProgressIndicator()
            }
            is com.example.gympulse.viewmodel.SplashState.Error -> {
                Text(
                    text = (splashState as com.example.gympulse.viewmodel.SplashState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {}
        }
    }
}
