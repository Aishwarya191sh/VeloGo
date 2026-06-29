package com.example.frontend.ui.screens.vendor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.domain.model.Vehicle
import com.example.frontend.domain.model.VendorBooking
import com.example.frontend.domain.repository.SearchRepository
import com.example.frontend.domain.repository.VendorRepository
import com.example.frontend.domain.usecase.*
import com.example.frontend.ui.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VendorViewModel @Inject constructor(
    private val getSessionUseCase: GetSessionUseCase,
    private val getVendorVehiclesUseCase: GetVendorVehiclesUseCase,
    private val addVehicleUseCase: AddVehicleUseCase,
    private val editVehicleUseCase: EditVehicleUseCase,
    private val deleteVehicleUseCase: DeleteVehicleUseCase,
    private val getVendorBookingsUseCase: GetVendorBookingsUseCase,
    private val searchRepository: SearchRepository,
    private val vendorRepository: VendorRepository
) : ViewModel() {

    // Current logged-in vendor user
    val currentSession = getSessionUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _vehiclesState = MutableStateFlow<Resource<List<Vehicle>>>(Resource.Success(emptyList()))
    val vehiclesState: StateFlow<Resource<List<Vehicle>>> = _vehiclesState.asStateFlow()

    private val _bookingsState = MutableStateFlow<Resource<List<VendorBooking>>>(Resource.Success(emptyList()))
    val bookingsState: StateFlow<Resource<List<VendorBooking>>> = _bookingsState.asStateFlow()

    // Add Vehicle State
    private val _addState = MutableStateFlow<Resource<String>>(Resource.Success(""))
    val addState: StateFlow<Resource<String>> = _addState.asStateFlow()

    // Edit Vehicle State
    private val _editState = MutableStateFlow<Resource<Vehicle?>>(Resource.Success(null))
    val editState: StateFlow<Resource<Vehicle?>> = _editState.asStateFlow()

    // Delete Vehicle State
    private val _deleteState = MutableStateFlow<Resource<String>>(Resource.Success(""))
    val deleteState: StateFlow<Resource<String>> = _deleteState.asStateFlow()

    // Change Booking Status State
    private val _changeStatusState = MutableStateFlow<Resource<String>>(Resource.Success(""))
    val changeStatusState: StateFlow<Resource<String>> = _changeStatusState.asStateFlow()

    // Metadata for Form Dropdowns
    private val _districts = MutableStateFlow<List<String>>(emptyList())
    val districts: StateFlow<List<String>> = _districts.asStateFlow()

    private val _locationsMap = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val locationsMap: StateFlow<Map<String, List<String>>> = _locationsMap.asStateFlow()

    private val _brands = MutableStateFlow<List<String>>(emptyList())
    val brands: StateFlow<List<String>> = _brands.asStateFlow()

    private val _models = MutableStateFlow<List<String>>(emptyList())
    val models: StateFlow<List<String>> = _models.asStateFlow()

    init {
        loadMetadata()
        loadVendorData()
    }

    fun loadVendorData() {
        viewModelScope.launch {
            currentSession.collect { user ->
                user?.let {
                    fetchVehicles(it.id)
                }
            }
        }
    }

    private fun loadMetadata() {
        viewModelScope.launch {
            searchRepository.getVehicleModels().onSuccess { list ->
                val districtsList = mutableListOf<String>()
                val locMap = mutableMapOf<String, MutableList<String>>()
                val brandsList = mutableListOf<String>()
                val modelsList = mutableListOf<String>()

                list.forEach { item ->
                    if (item.type == "location" && !item.district.isNullOrEmpty() && !item.location.isNullOrEmpty()) {
                        if (!districtsList.contains(item.district)) {
                            districtsList.add(item.district)
                        }
                        val locs = locMap.getOrPut(item.district) { mutableListOf() }
                        if (!locs.contains(item.location)) {
                            locs.add(item.location)
                        }
                    } else if (item.type == "car") {
                        item.brand?.let { if (!brandsList.contains(it)) brandsList.add(it) }
                        item.model?.let { if (!modelsList.contains(it)) modelsList.add(it) }
                    }
                }

                _districts.value = districtsList.sorted()
                _locationsMap.value = locMap
                _brands.value = brandsList.sorted()
                _models.value = modelsList.sorted()
            }.onFailure {
                // Fail silently or handle metadata loading failure
            }
        }
    }

    fun fetchVehicles(vendorId: String) {
        viewModelScope.launch {
            getVendorVehiclesUseCase(vendorId).collect { resource ->
                _vehiclesState.value = resource
                if (resource is Resource.Success) {
                    val vehicleIds = resource.data.map { it.id }
                    fetchBookings(vehicleIds)
                }
            }
        }
    }

    fun fetchBookings(vehicleIds: List<String>) {
        if (vehicleIds.isEmpty()) {
            _bookingsState.value = Resource.Success(emptyList())
            return
        }
        viewModelScope.launch {
            getVendorBookingsUseCase(vehicleIds).collect { resource ->
                _bookingsState.value = resource
            }
        }
    }

    fun addVehicle(
        registerationNumber: String,
        company: String,
        name: String,
        model: String,
        title: String,
        basePackage: String,
        price: Double,
        yearMade: Int,
        fuelType: String,
        description: String,
        seat: Int,
        transmitionType: String,
        insuranceEndDate: String,
        registrationEndDate: String,
        pollutionEndDate: String,
        carType: String,
        location: String,
        district: String,
        images: List<Pair<String, ByteArray>>
    ) {
        val vendorId = currentSession.value?.id ?: return
        viewModelScope.launch {
            addVehicleUseCase(
                registerationNumber = registerationNumber,
                company = company,
                name = name,
                model = model,
                title = title,
                basePackage = basePackage,
                price = price,
                yearMade = yearMade,
                fuelType = fuelType,
                description = description,
                seat = seat,
                transmitionType = transmitionType,
                insuranceEndDate = insuranceEndDate,
                registrationEndDate = registrationEndDate,
                pollutionEndDate = pollutionEndDate,
                carType = carType,
                location = location,
                district = district,
                addedBy = vendorId,
                images = images
            ).collect { resource ->
                _addState.value = resource
                if (resource is Resource.Success) {
                    fetchVehicles(vendorId)
                }
            }
        }
    }

    fun editVehicle(
        vehicleId: String,
        registerationNumber: String,
        company: String,
        name: String,
        model: String,
        title: String,
        basePackage: String,
        price: Double,
        yearMade: Int,
        description: String,
        seats: Int,
        transmitionType: String,
        registrationEndDate: String,
        insuranceEndDate: String,
        pollutionEndDate: String,
        carType: String,
        fuelType: String,
        location: String,
        district: String
    ) {
        val vendorId = currentSession.value?.id ?: return
        viewModelScope.launch {
            editVehicleUseCase(
                vehicleId = vehicleId,
                registerationNumber = registerationNumber,
                company = company,
                name = name,
                model = model,
                title = title,
                basePackage = basePackage,
                price = price,
                yearMade = yearMade,
                description = description,
                seats = seats,
                transmitionType = transmitionType,
                registrationEndDate = registrationEndDate,
                insuranceEndDate = insuranceEndDate,
                pollutionEndDate = pollutionEndDate,
                carType = carType,
                fuelType = fuelType,
                location = location,
                district = district
            ).collect { resource ->
                _editState.value = resource
                if (resource is Resource.Success) {
                    fetchVehicles(vendorId)
                }
            }
        }
    }

    fun deleteVehicle(vehicleId: String) {
        val vendorId = currentSession.value?.id ?: return
        viewModelScope.launch {
            deleteVehicleUseCase(vehicleId).collect { resource ->
                _deleteState.value = resource
                if (resource is Resource.Success) {
                    fetchVehicles(vendorId)
                }
            }
        }
    }

    fun changeBookingStatus(bookingId: String, status: String) {
        viewModelScope.launch {
            vendorRepository.changeBookingStatus(bookingId, status).collect { resource ->
                _changeStatusState.value = resource
                if (resource is Resource.Success) {
                    _vehiclesState.value.let { vehiclesRes ->
                        if (vehiclesRes is Resource.Success) {
                            fetchBookings(vehiclesRes.data.map { it.id })
                        }
                    }
                }
            }
        }
    }

    fun resetChangeStatusState() {
        _changeStatusState.value = Resource.Success("")
    }

    fun resetStates() {
        _addState.value = Resource.Success("")
        _editState.value = Resource.Success(null)
        _deleteState.value = Resource.Success("")
    }
}
