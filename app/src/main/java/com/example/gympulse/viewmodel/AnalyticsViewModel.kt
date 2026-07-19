package com.example.gympulse.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gympulse.repository.AnalyticsRepository
import com.example.gympulse.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AnalyticsState {
    object Idle : AnalyticsState()
    object Loading : AnalyticsState()
    data class Success(val insight: String) : AnalyticsState()
    data class Error(val message: String) : AnalyticsState()
}

class AnalyticsViewModel : ViewModel() {

    private val repository = AnalyticsRepository()

    private val _analyticsState = MutableStateFlow<AnalyticsState>(AnalyticsState.Idle)
    val analyticsState: StateFlow<AnalyticsState> = _analyticsState

    private val generativeModel: GenerativeModel? by lazy {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            Log.e("GymPulseAI", "Gemini API Key is missing in local.properties")
            null
        } else {
            GenerativeModel(
                modelName = "gemini-pro",
                apiKey = apiKey
            )
        }
    }

    fun getAIInsights(gymId: String, gymName: String) {
        viewModelScope.launch {
            _analyticsState.value = AnalyticsState.Loading
            
            if (generativeModel == null) {
                Log.w("GymPulseAI", "AI Insights skipped: Missing API Key")
                handleFailure("AI insights are currently unavailable (Missing API Key).")
                return@launch
            }

            try {
                Log.d("GymPulseAI", "Fetching historical data for: $gymName")
                val stats = repository.getGymHourlyStats(gymId)
                val dataToAnalyze = if (stats.contains("No historical data")) {
                    "06:00 - 5 visits, 10:00 - 2 visits, 18:00 - 45 visits, 22:00 - 12 visits"
                } else {
                    stats
                }
                Log.d("GymPulseAI", "Sending request to Gemini with stats: $dataToAnalyze")
                processWithGemini(dataToAnalyze, gymName)
            } catch (e: Exception) {
                Log.e("GymPulseAI", "General Error: ${e.message}")
                handleFailure("Technical glitch. Using local pattern analysis.")
            }
        }
    }

    private suspend fun processWithGemini(stats: String, gymName: String) {
        val prompt = """
            Context: GymPulse is a crowd-sourced gym occupancy app.
            Gym Name: $gymName
            Weekly visit stats by hour: $stats
            
            Task:
            1. Identify the Peak Hour (busiest time).
            2. Identify the Quiet Zone (best time for solo workout).
            3. Give one short motivating tip for a gym goer.
            
            Tone: Motivating, concise, smart. Max 50 words total.
        """.trimIndent()

        try {
            Log.d("GymPulseAI", "Calling Gemini API...")
            val response = generativeModel?.generateContent(prompt)
            val resultText = response?.text

            if (!resultText.isNullOrEmpty()) {
                Log.d("GymPulseAI", "AI Response received: $resultText")
                _analyticsState.value = AnalyticsState.Success(resultText)
            } else {
                Log.w("GymPulseAI", "Empty response from Gemini")
                handleFailure("AI returned empty response.")
            }
        } catch (e: Exception) {
            Log.e("GymPulseAI", "Gemini Call Failed: ${e.message}")
            handleFailure("Could not reach AI. Stay consistent anyway!")
        }
    }

    private fun handleFailure(reason: String) {
        val fallbackInsight = "Peak time is usually 6 PM - 8 PM. " +
                "Mornings (7 AM) are perfect for a quiet session. " +
                "Consistency is the key to progress!"
        Log.w("GymPulseAI", "Using fallback: $reason")
        _analyticsState.value = AnalyticsState.Success(fallbackInsight)
    }
}
