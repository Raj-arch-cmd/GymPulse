package com.example.gympulse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gympulse.model.User
import com.example.gympulse.util.Constants
import com.example.gympulse.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class GymSaveState {
    object Idle : GymSaveState()
    object Loading : GymSaveState()
    object Success : GymSaveState()
    data class Error(val message: String) : GymSaveState()
}

sealed class SplashState {
    object Loading : SplashState()
    object NavigateToLogin : SplashState()
    data class NavigateToOwnerHome(val user: User) : SplashState()
    data class NavigateToSelectGym(val user: User) : SplashState()
    data class NavigateToMemberHome(val user: User) : SplashState()
    data class Error(val message: String) : SplashState()
}

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _gymSaveState = MutableStateFlow<GymSaveState>(GymSaveState.Idle)
    val gymSaveState: StateFlow<GymSaveState> = _gymSaveState

    private val _splashState = MutableStateFlow<SplashState>(SplashState.Loading)
    val splashState: StateFlow<SplashState> = _splashState

    val isLoggedIn get() = repository.currentUser != null

    fun checkCurrentUser() {
        viewModelScope.launch {
            val currentUser = repository.currentUser
            if (currentUser == null) {
                _splashState.value = SplashState.NavigateToLogin
                return@launch
            }
            val user = repository.getUser(currentUser.uid)
            if (user == null) {
                _splashState.value = SplashState.NavigateToLogin
                return@launch
            }
            _splashState.value = when (user.role) {
                Constants.ROLE_OWNER -> SplashState.NavigateToOwnerHome(user)
                Constants.ROLE_MEMBER -> {
                    if (user.gymId.isNullOrBlank()) {
                        SplashState.NavigateToSelectGym(user)
                    } else {
                        SplashState.NavigateToMemberHome(user)
                    }
                }
                else -> SplashState.NavigateToLogin
            }
        }
    }

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.loginWithEmail(email, password)
            if (result.isSuccess) {
                val uid = repository.currentUser?.uid ?: ""
                val role = repository.getUserRole(uid)
                _authState.value = AuthState.Success(role)
            } else {
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Login failed"
                )
            }
        }
    }

    fun registerWithEmail(name: String, email: String, password: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.registerWithEmail(name, email, password, role)
            if (result.isSuccess) {
                _authState.value = AuthState.Success(role)
            } else {
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Registration failed"
                )
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.loginWithGoogle(idToken)
            if (result.isSuccess) {
                val uid = repository.currentUser?.uid ?: ""
                val role = repository.getUserRole(uid)
                _authState.value = AuthState.Success(role)
            } else {
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Google login failed"
                )
            }
        }
    }

    fun logout() {
        repository.logout()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun saveSelectedGym(gymId: String) {
        viewModelScope.launch {
            val uid = repository.currentUser?.uid
            if (uid.isNullOrBlank()) {
                _gymSaveState.value = GymSaveState.Error("User not authenticated")
                return@launch
            }
            _gymSaveState.value = GymSaveState.Loading
            val result = repository.updateUserGymId(uid, gymId)
            _gymSaveState.value = if (result.isSuccess) {
                GymSaveState.Success
            } else {
                GymSaveState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to save gym selection"
                )
            }
        }
    }

    fun resetGymSaveState() {
        _gymSaveState.value = GymSaveState.Idle
    }
}