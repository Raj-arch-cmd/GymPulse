package com.example.gympulse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gympulse.model.Gym
import com.example.gympulse.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class GymState {
    object Idle : GymState()
    object Loading : GymState()
    data class Success(val message: String) : GymState()
    data class Error(val message: String) : GymState()
}

class GymViewModel : ViewModel() {

    private val repository = GymRepository()

    private val _gymState = MutableStateFlow<GymState>(GymState.Idle)
    val gymState: StateFlow<GymState> = _gymState

    private val _gyms = MutableStateFlow<List<Gym>>(emptyList())
    val gyms: StateFlow<List<Gym>> = _gyms

    private val _selectedGym = MutableStateFlow<Gym?>(null)
    val selectedGym: StateFlow<Gym?> = _selectedGym

    private val _liveCount = MutableStateFlow(0)
    val liveCount: StateFlow<Int> = _liveCount

    fun registerGym(name: String, address: String, latitude: Double,
                    longitude: Double, ownerId: String) {
        viewModelScope.launch {
            _gymState.value = GymState.Loading
            val gym = Gym(
                name = name,
                address = address,
                latitude = latitude,
                longitude = longitude,
                ownerId = ownerId
            )
            val result = repository.registerGym(gym)
            if (result.isSuccess) {
                _gymState.value = GymState.Success("Gym registered successfully!")
            } else {
                _gymState.value = GymState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to register gym"
                )
            }
        }
    }

    fun loadAllGyms() {
        viewModelScope.launch {
            _gymState.value = GymState.Loading
            val result = repository.getAllGyms()
            if (result.isSuccess) {
                _gyms.value = result.getOrDefault(emptyList())
                _gymState.value = GymState.Idle
            } else {
                _gymState.value = GymState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to load gyms"
                )
            }
        }
    }

    fun selectGym(gym: Gym) {
        _selectedGym.value = gym
        listenToLiveCount(gym.gymId)
    }

    private fun listenToLiveCount(gymId: String) {
        repository.listenToGymCount(gymId) { count ->
            _liveCount.value = count
        }
    }

    fun resetState() {
        _gymState.value = GymState.Idle
    }
    fun loadGymById(gymId: String) {
        viewModelScope.launch {
            val result = repository.getGymById(gymId)
            if (result.isSuccess) {
                val gym = result.getOrNull()!!
                _selectedGym.value = gym
                listenToLiveCount(gymId)
            }
        }
    }
}
