package com.example.frontend.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.domain.model.Vehicle
import com.example.frontend.domain.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VehicleDetailsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val vehicle: Vehicle? = null
)

@HiltViewModel
class VehicleDetailsViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehicleDetailsUiState())
    val uiState: StateFlow<VehicleDetailsUiState> = _uiState.asStateFlow()

    fun loadVehicleDetails(vehicleId: String, cachedVehicle: Vehicle?) {
        // If cached vehicle matches the ID, use it directly to avoid API roundtrip
        if (cachedVehicle != null && cachedVehicle.id == vehicleId) {
            _uiState.update {
                it.copy(
                    vehicle = cachedVehicle,
                    isLoading = false,
                    error = null
                )
            }
            return
        }

        // Fetch from repository
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            searchRepository.getVehicleDetails(vehicleId)
                .onSuccess { vehicle ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            vehicle = vehicle
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.localizedMessage ?: "Failed to load vehicle details"
                        )
                    }
                }
        }
    }
}

