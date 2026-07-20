package com.example.gympulse.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onMemberSuccess: () -> Unit,
    onOwnerSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(Constants.ROLE_MEMBER) }
    var errorMessage by remember { mutableStateOf("") }

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Logo
            Text(
                text = "Gym",
                fontSize = 36.sp,
                fontWeight = FontWeight.Light,
                color = Color.White
            )
            Text(
                text = "Pulse",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5A0)
            )
            Text(
                text = "Create your account",
                fontSize = 14.sp,
                color = Color(0xFF888888),
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Role selector
            Text(
                text = "I am a",
                fontSize = 14.sp,
                color = Color(0xFF888888),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 10.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Member option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selectedRole == Constants.ROLE_MEMBER)
                                Color(0xFF00E5A0).copy(alpha = 0.15f)
                            else Color(0xFF1A1A1A)
                        )
                        .border(
                            width = if (selectedRole == Constants.ROLE_MEMBER) 1.5.dp else 0.5.dp,
                            color = if (selectedRole == Constants.ROLE_MEMBER)
                                Color(0xFF00E5A0) else Color(0xFF333333),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedRole = Constants.ROLE_MEMBER },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Gym Member",
                        color = if (selectedRole == Constants.ROLE_MEMBER)
                            Color(0xFF00E5A0) else Color(0xFF888888),
                        fontWeight = if (selectedRole == Constants.ROLE_MEMBER)
                            FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }

                // Owner option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selectedRole == Constants.ROLE_OWNER)
                                Color(0xFF00E5A0).copy(alpha = 0.15f)
                            else Color(0xFF1A1A1A)
                        )
                        .border(
                            width = if (selectedRole == Constants.ROLE_OWNER) 1.5.dp else 0.5.dp,
                            color = if (selectedRole == Constants.ROLE_OWNER)
                                Color(0xFF00E5A0) else Color(0xFF333333),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedRole = Constants.ROLE_OWNER },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Gym Owner",
                        color = if (selectedRole == Constants.ROLE_OWNER)
                            Color(0xFF00E5A0) else Color(0xFF888888),
                        fontWeight = if (selectedRole == Constants.ROLE_OWNER)
                            FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full name", color = Color(0xFF888888)) },
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

            Spacer(modifier = Modifier.height(14.dp))

            // Confirm password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm password", color = Color(0xFF888888)) },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Text(
                            text = if (confirmPasswordVisible) "Hide" else "Show",
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

            // Register button
            Button(
                onClick = {
                    errorMessage = ""
                    when {
                        name.isBlank() -> errorMessage = "Please enter your name"
                        email.isBlank() -> errorMessage = "Please enter your email"
                        password.isBlank() -> errorMessage = "Please enter a password"
                        password.length < 6 -> errorMessage = "Password must be at least 6 characters"
                        password != confirmPassword -> errorMessage = "Passwords do not match"
                        else -> viewModel.registerWithEmail(name, email, password, selectedRole)
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
                        text = "Create Account",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Login link
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Already have an account? ",
                    color = Color(0xFF888888),
                    fontSize = 14.sp
                )
                TextButton(onClick = {
                    viewModel.resetState()
                    onNavigateToLogin()
                }) {
                    Text(
                        text = "Login",
                        color = Color(0xFF00E5A0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}