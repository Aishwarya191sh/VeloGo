package com.example.frontend.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.domain.model.Vehicle
import com.example.frontend.domain.model.VendorBooking
import com.example.frontend.domain.repository.AdminRepository
import com.example.frontend.domain.usecase.GetSessionUseCase
import com.example.frontend.ui.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val getSessionUseCase: GetSessionUseCase,
    private val adminRepository: AdminRepository
) : ViewModel() {

    val currentSession = getSessionUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _vendorRequestsState = MutableStateFlow<Resource<List<Vehicle>>>(Resource.Success(emptyList()))
    val vendorRequestsState: StateFlow<Resource<List<Vehicle>>> = _vendorRequestsState.asStateFlow()

    private val _approvedVehiclesState = MutableStateFlow<Resource<List<Vehicle>>>(Resource.Success(emptyList()))
    val approvedVehiclesState: StateFlow<Resource<List<Vehicle>>> = _approvedVehiclesState.asStateFlow()

    private val _bookingsState = MutableStateFlow<Resource<List<VendorBooking>>>(Resource.Success(emptyList()))
    val bookingsState: StateFlow<Resource<List<VendorBooking>>> = _bookingsState.asStateFlow()

    // Action states
    private val _approveState = MutableStateFlow<Resource<String>>(Resource.Success(""))
    val approveState: StateFlow<Resource<String>> = _approveState.asStateFlow()

    private val _rejectState = MutableStateFlow<Resource<String>>(Resource.Success(""))
    val rejectState: StateFlow<Resource<String>> = _rejectState.asStateFlow()

    private val _deleteState = MutableStateFlow<Resource<String>>(Resource.Success(""))
    val deleteState: StateFlow<Resource<String>> = _deleteState.asStateFlow()

    private val _changeStatusState = MutableStateFlow<Resource<String>>(Resource.Success(""))
    val changeStatusState: StateFlow<Resource<String>> = _changeStatusState.asStateFlow()

    init {
        loadAdminData()
    }

    fun loadAdminData() {
        fetchVendorRequests()
        fetchApprovedVehicles()
        fetchBookings()
    }

    fun fetchVendorRequests() {
        viewModelScope.launch {
            adminRepository.fetchVendorVehicleRequests().collect { resource ->
                _vendorRequestsState.value = resource
            }
        }
    }

    fun fetchApprovedVehicles() {
        viewModelScope.launch {
            adminRepository.getShowVehicles().collect { resource ->
                _approvedVehiclesState.value = resource
            }
        }
    }

    fun fetchBookings() {
        viewModelScope.launch {
            adminRepository.getAllBookings().collect { resource ->
                _bookingsState.value = resource
            }
        }
    }

    fun approveVehicleRequest(id: String) {
        viewModelScope.launch {
            adminRepository.approveVendorVehicleRequest(id).collect { resource ->
                _approveState.value = resource
                if (resource is Resource.Success) {
                    loadAdminData()
                }
            }
        }
    }

    fun rejectVehicleRequest(id: String) {
        viewModelScope.launch {
            adminRepository.rejectVendorVehicleRequest(id).collect { resource ->
                _rejectState.value = resource
                if (resource is Resource.Success) {
                    loadAdminData()
                }
            }
        }
    }

    fun deleteVehicle(id: String) {
        viewModelScope.launch {
            adminRepository.deleteVehicle(id).collect { resource ->
                _deleteState.value = resource
                if (resource is Resource.Success) {
                    loadAdminData()
                }
            }
        }
    }

    fun changeBookingStatus(bookingId: String, status: String) {
        viewModelScope.launch {
            adminRepository.changeBookingStatus(bookingId, status).collect { resource ->
                _changeStatusState.value = resource
                if (resource is Resource.Success) {
                    fetchBookings()
                }
            }
        }
    }

    fun resetActionStates() {
        _approveState.value = Resource.Success("")
        _rejectState.value = Resource.Success("")
        _deleteState.value = Resource.Success("")
        _changeStatusState.value = Resource.Success("")
    }
}
