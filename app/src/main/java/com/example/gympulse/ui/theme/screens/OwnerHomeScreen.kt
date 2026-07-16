package com.example.gympulse.ui.theme.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gympulse.viewmodel.AuthViewModel
import com.example.gympulse.viewmodel.GymState
import com.example.gympulse.viewmodel.GymViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("MissingPermission")
@Composable
fun OwnerHomeScreen(
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    gymViewModel: GymViewModel = viewModel()
) {
    val gymState by gymViewModel.gymState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var gymName by remember { mutableStateOf("") }
    var gymAddress by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var currentLat by remember { mutableStateOf(0.0) }
    var currentLng by remember { mutableStateOf(0.0) }
    var locationFetched by remember { mutableStateOf(false) }
    var locationStatus by remember { mutableStateOf("Tap Register to auto-detect location") }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Function to get current location
    fun fetchLocation(onSuccess: (Double, Double) -> Unit) {
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            if (location != null) {
                currentLat = location.latitude
                currentLng = location.longitude
                locationFetched = true
                locationStatus = "Location detected: ${
                    String.format("%.4f", location.latitude)
                }, ${
                    String.format("%.4f", location.longitude)
                }"
                onSuccess(location.latitude, location.longitude)
            } else {
                errorMessage = "Could not get location. Please try again."
            }
        }.addOnFailureListener {
            errorMessage = "Location error: ${it.message}"
        }
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted) {
            fetchLocation { lat, lng ->
                val ownerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                gymViewModel.registerGym(
                    name = gymName,
                    address = gymAddress,
                    latitude = lat,
                    longitude = lng,
                    ownerId = ownerId
                )
            }
        } else {
            errorMessage = "Location permission is required to register gym"
        }
    }

    LaunchedEffect(gymState) {
        when (val state = gymState) {
            is GymState.Success -> {
                successMessage = state.message
                gymName = ""
                gymAddress = ""
                locationFetched = false
                locationStatus = "Tap Register to auto-detect location"
                gymViewModel.resetState()
            }
            is GymState.Error -> {
                errorMessage = state.message
                gymViewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Owner Dashboard",
                        fontSize = 14.sp,
                        color = Color(0xFF888888)
                    )
                    Text(
                        text = "GymPulse",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                TextButton(onClick = {
                    authViewModel.logout()
                    onLogout()
                }) {
                    Text(
                        text = "Logout",
                        color = Color(0xFFFF5252),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Register gym card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Register your gym",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Add your gym so members can find and check in",
                        fontSize = 13.sp,
                        color = Color(0xFF888888),
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    // Gym name
                    OutlinedTextField(
                        value = gymName,
                        onValueChange = { gymName = it },
                        label = { Text("Gym name", color = Color(0xFF888888)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5A0),
                            unfocusedBorderColor = Color(0xFF333333),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF00E5A0)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Gym address
                    OutlinedTextField(
                        value = gymAddress,
                        onValueChange = { gymAddress = it },
                        label = { Text("Gym address", color = Color(0xFF888888)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5A0),
                            unfocusedBorderColor = Color(0xFF333333),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF00E5A0)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Location status card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (locationFetched)
                                Color(0xFF00E5A0).copy(alpha = 0.1f)
                            else Color(0xFF2A2A2A)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (locationFetched)
                                            Color(0xFF00E5A0)
                                        else Color(0xFF888888),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = locationStatus,
                                fontSize = 12.sp,
                                color = if (locationFetched)
                                    Color(0xFF00E5A0) else Color(0xFF888888),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Success message
                    if (successMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = successMessage,
                            color = Color(0xFF00E5A0),
                            fontSize = 13.sp
                        )
                    }

                    // Error message
                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage,
                            color = Color(0xFFFF5252),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Register button
                    Button(
                        onClick = {
                            errorMessage = ""
                            successMessage = ""
                            when {
                                gymName.isBlank() ->
                                    errorMessage = "Please enter gym name"
                                gymAddress.isBlank() ->
                                    errorMessage = "Please enter gym address"
                                else -> {
                                    locationStatus = "Detecting your location..."
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5A0)
                        ),
                        enabled = gymState !is GymState.Loading
                    ) {
                        if (gymState is GymState.Loading) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Register Gym",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // What's next card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "What's next?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf(
                        "Members can search and find your gym",
                        "Live occupancy counter goes live",
                        "See peak hours analytics dashboard",
                        "Track most active members"
                    ).forEach { item ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•  ",
                                color = Color(0xFF00E5A0),
                                fontSize = 14.sp
                            )
                            Text(
                                text = item,
                                color = Color(0xFF888888),
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}



