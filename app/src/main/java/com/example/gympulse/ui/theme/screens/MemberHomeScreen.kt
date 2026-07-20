package com.example.gympulse.ui.theme.screens

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gympulse.geofence.GeofenceManager
import com.example.gympulse.viewmodel.AnalyticsState
import com.example.gympulse.viewmodel.AnalyticsViewModel
import com.example.gympulse.viewmodel.AuthViewModel
import com.example.gympulse.viewmodel.GymViewModel
import com.example.gympulse.viewmodel.SessionState
import com.example.gympulse.viewmodel.SessionViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MemberHomeScreen(
    gymId: String,
    onLogout: () -> Unit,
    onChangeGym: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    gymViewModel: GymViewModel = viewModel(),
    sessionViewModel: SessionViewModel = viewModel(),
    analyticsViewModel: AnalyticsViewModel = viewModel()
) {
    val selectedGym by gymViewModel.selectedGym.collectAsStateWithLifecycle()
    val liveCount by gymViewModel.liveCount.collectAsStateWithLifecycle()
    val crowdLevel by gymViewModel.crowdLevel.collectAsStateWithLifecycle()
    val showOverCapacityWarning by gymViewModel.showOverCapacityWarning.collectAsStateWithLifecycle()
    val isCheckedIn by sessionViewModel.isCheckedIn.collectAsStateWithLifecycle()
    val sessionState by sessionViewModel.sessionState.collectAsStateWithLifecycle()
    val visitCount by sessionViewModel.visitCount.collectAsStateWithLifecycle()
    val aiState by analyticsViewModel.analyticsState.collectAsStateWithLifecycle()

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val userName = FirebaseAuth.getInstance().currentUser?.displayName
        ?: FirebaseAuth.getInstance().currentUser?.email?.substringBefore("@")
        ?: "Member"

    val context = LocalContext.current
    val geofenceManager = remember { GeofenceManager(context) }

    var snackMessage by remember { mutableStateOf("") }

    // Permissions logic
    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) snackMessage = "Auto check-out enabled!"
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    // Load Data
    LaunchedEffect(gymId) {
        if (gymId.isNotEmpty()) gymViewModel.loadGymById(gymId)
    }

    LaunchedEffect(selectedGym) {
        selectedGym?.let { gym ->
            analyticsViewModel.getAIInsights(gym.gymId, gym.name)
            sessionViewModel.listenToActiveSession(userId, gym.gymId)
            sessionViewModel.loadVisitCount(userId)
            geofenceManager.addGeofence(gym.gymId, gym.latitude, gym.longitude)
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D0D))) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Welcome back", fontSize = 14.sp, color = Color(0xFF888888))
                    Text(userName.split(" ").firstOrNull() ?: "Member", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                TextButton(onClick = {
                    geofenceManager.removeAllGeofences()
                    authViewModel.logout()
                    onLogout()
                }) {
                    Text("Logout", color = Color(0xFFFF5252), fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gym Banner
            selectedGym?.let { gym ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(gym.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(gym.address, fontSize = 12.sp, color = Color(0xFF888888))
                        }
                        TextButton(onClick = { geofenceManager.removeAllGeofences(); onChangeGym() }) {
                            Text("Change", color = Color(0xFF00E5A0), fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Occupancy Card
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(crowdLevel, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(if (selectedGym != null) "$liveCount Members" else "-- Members", fontSize = 16.sp, color = Color(0xFF888888))

                    if (showOverCapacityWarning) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "⚠️ Above recommended capacity",
                            fontSize = 13.sp,
                            color = Color(0xFFFF5252),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- AI INSIGHTS CARD PLACEMENT ---
            GeminiInsightCard(
                state = aiState,
                onRetry = { selectedGym?.let { analyticsViewModel.getAIInsights(it.gymId, it.name) } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            Button(
                onClick = {
                    val gId = selectedGym?.gymId ?: return@Button
                    if (isCheckedIn) sessionViewModel.checkOut(userId, gId) else sessionViewModel.checkIn(userId, gId)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isCheckedIn) Color(0xFFFF5252) else Color(0xFF00E5A0)),
                enabled = selectedGym != null && sessionState !is SessionState.Loading
            ) {
                Text(if (isCheckedIn) "Check Out" else "Check In", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Streak", "0", Modifier.weight(1f))
                StatCard("Points", "0", Modifier.weight(1f))
                StatCard("Visits", "$visitCount", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5A0))
            Text(label, fontSize = 12.sp, color = Color(0xFF888888))
        }
    }
}

@Composable
fun GeminiInsightCard(state: AnalyticsState, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = BorderStroke(1.dp, Color(0xFF00E5A0).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(Color(0xFF00E5A0), CircleShape))
                Spacer(modifier = Modifier.width(10.dp))
                Text("GymPulse AI Insights", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5A0))
            }
            Spacer(modifier = Modifier.height(12.dp))
            when (state) {
                is AnalyticsState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(CircleShape), color = Color(0xFF00E5A0))
                is AnalyticsState.Success -> Text(state.insight, fontSize = 15.sp, color = Color.White, lineHeight = 22.sp)
                is AnalyticsState.Error -> {
                    Text(state.message, color = Color(0xFFFF5252))
                    TextButton(onClick = onRetry) { Text("Retry", color = Color(0xFF00E5A0)) }
                }
                else -> {}
            }
        }
    }
}