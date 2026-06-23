package com.abhijit.footlog.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abhijit.footlog.data.entity.ExploredCellEntity
import com.abhijit.footlog.data.entity.SessionEntity
import com.abhijit.footlog.data.repository.SessionRepository
import com.abhijit.footlog.util.cellCenterLatLng
import kotlinx.coroutines.flow.*

class RoutesViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SessionRepository(app)

    val favoriteRoutes: StateFlow<List<SessionEntity>> = repo.getFavoriteSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exploredCells: StateFlow<List<ExploredCellEntity>> = repo.getAllExploredCells()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Center of all explored cells — used to position the explore map camera
    val exploreCenter: StateFlow<Pair<Double, Double>?> = exploredCells
        .map { cells ->
            if (cells.isEmpty()) return@map null
            val avgX = cells.map { it.cellX }.average()
            val avgY = cells.map { it.cellY }.average()
            cellCenterLatLng(avgX.toInt(), avgY.toInt())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>,
                extras: androidx.lifecycle.viewmodel.CreationExtras
            ): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                return RoutesViewModel(app) as T
            }
        }
    }
}
