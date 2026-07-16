package com.example.gympulse.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gympulse.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SessionState {
    object Idle : SessionState()
    object Loading : SessionState()
    object CheckedIn : SessionState()
    object CheckedOut : SessionState()
    data class Error(val message: String) : SessionState()
}

class SessionViewModel : ViewModel() {

    private val repository = SessionRepository()

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Idle)
    val sessionState: StateFlow<SessionState> = _sessionState

    private val _isCheckedIn = MutableStateFlow(false)
    val isCheckedIn: StateFlow<Boolean> = _isCheckedIn

    private val _visitCount = MutableStateFlow(0)
    val visitCount: StateFlow<Int> = _visitCount

    /**
     * Sets up the real-time observer for the user's session status.
     * When the Repository emits a change, this StateFlow updates the UI.
     */
    fun listenToActiveSession(userId: String, gymId: String) {
        viewModelScope.launch {
            repository.listenToActiveSession(userId, gymId)
                .collect { hasActiveSession ->
                    _isCheckedIn.value = hasActiveSession
                    Log.d("GymPulse", "UI State Updated: isCheckedIn = $hasActiveSession")
                }
        }
    }

    fun loadVisitCount(userId: String) {
        viewModelScope.launch {
            _visitCount.value = repository.getUserSessionCount(userId)
        }
    }

    fun checkIn(userId: String, gymId: String) {
        viewModelScope.launch {
            _sessionState.value = SessionState.Loading
            val result = repository.checkIn(userId, gymId)
            if (result.isSuccess) {
                // We don't manually set _isCheckedIn = true here
                // because the Flow listener will pick it up automatically!
                _sessionState.value = SessionState.CheckedIn
            } else {
                _sessionState.value = SessionState.Error(
                    result.exceptionOrNull()?.message ?: "Check in failed"
                )
            }
        }
    }

    fun checkOut(userId: String, gymId: String, silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _sessionState.value = SessionState.Loading
            val result = repository.checkOut(userId, gymId)
            if (result.isSuccess) {
                if (!silent) _sessionState.value = SessionState.CheckedOut
            } else {
                if (!silent) {
                    _sessionState.value = SessionState.Error(
                        result.exceptionOrNull()?.message ?: "Check out failed"
                    )
                }
            }
        }
    }

    fun resetState() {
        _sessionState.value = SessionState.Idle
    }
}