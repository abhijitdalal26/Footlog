package com.abhijit.footlog.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abhijit.footlog.data.repository.SessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StatsData(
    val totalDistanceMeters: Float = 0f,
    val totalSessions: Int = 0,
    val currentStreak: Int = 0,
    val exploredAreaKm2: Double = 0.0,
    val weeklyDistances: List<Float> = List(7) { 0f }
)

class StatsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SessionRepository(app)
    private val _stats = MutableStateFlow(StatsData())
    val stats: StateFlow<StatsData> = _stats.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getSessionCount().collect { count ->
                val streak = repo.getCurrentStreak()
                val weekly = repo.getWeeklyDistances()
                _stats.update { it.copy(totalSessions = count, currentStreak = streak, weeklyDistances = weekly) }
            }
        }
        viewModelScope.launch {
            combine(
                repo.getTotalDistance(),
                repo.getExploredCellCount()
            ) { dist, cells ->
                Pair(dist ?: 0f, cells)
            }.collect { (dist, cells) ->
                // Each cell is ~25m x 25m = 625m² = 0.000625 km²
                _stats.update { it.copy(totalDistanceMeters = dist, exploredAreaKm2 = cells * 0.000625) }
            }
        }
    }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>,
                extras: androidx.lifecycle.viewmodel.CreationExtras
            ): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                return StatsViewModel(app) as T
            }
        }
    }
}
