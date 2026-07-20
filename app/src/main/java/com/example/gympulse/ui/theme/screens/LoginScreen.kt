package com.example.gympulse.ui.theme.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gympulse.util.Constants
import com.example.gympulse.viewmodel.AuthState
import com.example.gympulse.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onMemberSuccess: () -> Unit,
    onOwnerSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Google Sign In launcher
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("699736008988-9btd23fngj5o8nk2beufasca4ngbu2bd.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { viewModel.loginWithGoogle(it) }
            } catch (e: ApiException) {
                errorMessage = "Google sign in failed"
            }
        }
    }

    // Handle auth state changes
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                if (state.role == Constants.ROLE_OWNER) onOwnerSuccess()
                else onMemberSuccess()
                viewModel.resetState()
            }
            is AuthState.Error -> {
                errorMessage = state.message
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
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Logo area
            Text(
                text = "Gym",
                fontSize = 42.sp,
                fontWeight = FontWeight.Light,
                color = Color.White
            )
            Text(
                text = "Pulse",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5A0)
            )
            Text(
                text = "Know before you go",
                fontSize = 14.sp,
                color = Color(0xFF888888),
                modifier = Modifier.padding(top = 4.dp, bottom = 40.dp)
            )

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = Color(0xFF888888)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color(0xFF888888)) },
                singleLine = true,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text = if (passwordVisible) "Hide" else "Show",
                            color = Color(0xFF00E5A0),
                            fontSize = 12.sp
                        )
                    }
                },
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

            // Error message
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF5252),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login button
            Button(
                onClick = {
                    errorMessage = ""
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill in all fields"
                    } else {
                        viewModel.loginWithEmail(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5A0)
                ),
                enabled = authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Login",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color(0xFF333333))
                Text(
                    text = "  or  ",
                    color = Color(0xFF888888),
                    fontSize = 13.sp
                )
                Divider(modifier = Modifier.weight(1f), color = Color(0xFF333333))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Sign In button
            OutlinedButton(
                onClick = {
                    errorMessage = ""
                    googleSignInClient.signOut().addOnCompleteListener {
                        googleLauncher.launch(googleSignInClient.signInIntent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF333333))
                ),
                enabled = authState !is AuthState.Loading
            ) {
                Text(
                    text = "G   Continue with Google",
                    color = Color.White,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Register link
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Don't have an account? ",
                    color = Color(0xFF888888),
                    fontSize = 14.sp
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = "Sign up",
                        color = Color(0xFF00E5A0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}