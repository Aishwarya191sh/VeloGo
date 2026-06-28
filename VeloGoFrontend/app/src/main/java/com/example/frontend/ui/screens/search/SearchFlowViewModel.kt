package com.example.frontend.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.remote.dto.SearchVehiclesRequest
import com.example.frontend.data.remote.dto.VariantsRequest
import com.example.frontend.domain.model.SearchFilters
import com.example.frontend.domain.model.SortOption
import com.example.frontend.domain.model.Vehicle
import com.example.frontend.domain.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

data class SearchUiState(
    val isLoadingLocations: Boolean = false,
    val districts: List<String> = emptyList(),
    val locationsMap: Map<String, List<String>> = emptyMap(),
    val selectedDistrict: String = "",
    val pickupLocation: String = "",
    val dropoffLocation: String = "",
    val pickupDateTime: Calendar? = null,
    val dropoffDateTime: Calendar? = null,
    
    val isSearching: Boolean = false,
    val searchError: String? = null,
    val availableModels: List<Vehicle> = emptyList(),
    
    val selectedModel: Vehicle? = null,
    val isLoadingVariants: Boolean = false,
    val variantsError: String? = null,
    val rawVariants: List<Vehicle> = emptyList(),
    val filteredVariants: List<Vehicle> = emptyList(),
    val activeFilters: SearchFilters = SearchFilters(),
    val activeSort: SortOption = SortOption.NONE,

    // Reserved selection for details module
    val selectedVariant: Vehicle? = null
)

@HiltViewModel
class SearchFlowViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    init {
        fetchLocationsLOV()
    }

    fun fetchLocationsLOV() {
        _uiState.update { it.copy(isLoadingLocations = true) }
        viewModelScope.launch {
            searchRepository.getVehicleModels()
                .onSuccess { list ->
                    val districtsList = mutableListOf<String>()
                    val map = mutableMapOf<String, MutableList<String>>()
                    
                    list.forEach { item ->
                        if (item.type == "location" && !item.district.isNullOrEmpty() && !item.location.isNullOrEmpty()) {
                            if (!districtsList.contains(item.district)) {
                                districtsList.add(item.district)
                            }
                            val locs = map.getOrPut(item.district) { mutableListOf() }
                            if (!locs.contains(item.location)) {
                                locs.add(item.location)
                            }
                        }
                    }

                    _uiState.update {
                        it.copy(
                            isLoadingLocations = false,
                            districts = districtsList.sorted(),
                            locationsMap = map,
                            selectedDistrict = districtsList.firstOrNull() ?: "",
                            pickupLocation = map[districtsList.firstOrNull()]?.firstOrNull() ?: "",
                            dropoffLocation = map[districtsList.firstOrNull()]?.firstOrNull() ?: ""
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingLocations = false,
                            searchError = error.localizedMessage ?: "Failed to load locations metadata"
                        )
                    }
                }
        }
    }

    fun setDistrict(district: String) {
        val locations = _uiState.value.locationsMap[district] ?: emptyList()
        _uiState.update {
            it.copy(
                selectedDistrict = district,
                pickupLocation = locations.firstOrNull() ?: "",
                dropoffLocation = locations.firstOrNull() ?: ""
            )
        }
    }

    fun setPickupLocation(location: String) {
        _uiState.update { it.copy(pickupLocation = location) }
    }

    fun setDropoffLocation(location: String) {
        _uiState.update { it.copy(dropoffLocation = location) }
    }

    fun setPickupDateTime(dateTime: Calendar?) {
        _uiState.update { it.copy(pickupDateTime = dateTime) }
        validateDates()
    }

    fun setDropoffDateTime(dateTime: Calendar?) {
        _uiState.update { it.copy(dropoffDateTime = dateTime) }
        validateDates()
    }

    private fun validateDates(): Boolean {
        val pickup = _uiState.value.pickupDateTime
        val dropoff = _uiState.value.dropoffDateTime
        if (pickup == null || dropoff == null) {
            return false
        }
        
        val diffMs = dropoff.timeInMillis - pickup.timeInMillis
        val diffHours = diffMs / (1000 * 60 * 60)
        
        return if (diffHours < 24) {
            _uiState.update { it.copy(searchError = "Drop-off time must be at least 24 hours after Pick-up time") }
            false
        } else {
            _uiState.update { it.copy(searchError = null) }
            true
        }
    }

    fun searchVehicles(onSuccess: () -> Unit) {
        if (!validateDates()) return

        val state = _uiState.value
        val pickupStr = isoFormatter.format(state.pickupDateTime!!.time)
        val dropoffStr = isoFormatter.format(state.dropoffDateTime!!.time)

        val request = SearchVehiclesRequest(
            pickupDate = pickupStr,
            dropOffDate = dropoffStr,
            pickUpDistrict = state.selectedDistrict,
            pickUpLocation = state.pickupLocation
        )

        _uiState.update { it.copy(isSearching = true, searchError = null) }
        viewModelScope.launch {
            searchRepository.searchUniqueModels(request)
                .onSuccess { models ->
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            availableModels = models
                        )
                    }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            searchError = error.localizedMessage ?: "Failed to find available vehicles"
                        )
                    }
                }
        }
    }

    fun selectModel(model: Vehicle, onSuccess: () -> Unit) {
        _uiState.update {
            it.copy(
                selectedModel = model,
                activeFilters = SearchFilters(),
                activeSort = SortOption.NONE,
                rawVariants = emptyList(),
                filteredVariants = emptyList()
            )
        }
        fetchVariantsForSelectedModel(onSuccess)
    }

    fun fetchVariantsForSelectedModel(onSuccess: () -> Unit = {}) {
        val state = _uiState.value
        val model = state.selectedModel ?: return

        val pickupStr = isoFormatter.format(state.pickupDateTime!!.time)
        val dropoffStr = isoFormatter.format(state.dropoffDateTime!!.time)

        val request = VariantsRequest(
            pickUpDistrict = state.selectedDistrict,
            pickUpLocation = state.pickupLocation,
            pickupDate = pickupStr,
            dropOffDate = dropoffStr,
            model = model.model ?: ""
        )

        _uiState.update { it.copy(isLoadingVariants = true, variantsError = null) }
        viewModelScope.launch {
            searchRepository.getVehicleVariants(request)
                .onSuccess { variants ->
                    _uiState.update {
                        it.copy(
                            isLoadingVariants = false,
                            rawVariants = variants,
                            filteredVariants = applyFiltersAndSort(variants, it.activeFilters, it.activeSort)
                        )
                    }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingVariants = false,
                            variantsError = error.localizedMessage ?: "Failed to load variants"
                        )
                    }
                }
        }
    }

    fun updateFilters(filters: SearchFilters) {
        _uiState.update {
            it.copy(
                activeFilters = filters,
                filteredVariants = applyFiltersAndSort(it.rawVariants, filters, it.activeSort)
            )
        }
    }

    fun updateSort(sort: SortOption) {
        _uiState.update {
            it.copy(
                activeSort = sort,
                filteredVariants = applyFiltersAndSort(it.rawVariants, it.activeFilters, sort)
            )
        }
    }

    fun selectVariant(variant: Vehicle) {
        _uiState.update { it.copy(selectedVariant = variant) }
    }

    private fun applyFiltersAndSort(
        variants: List<Vehicle>,
        filters: SearchFilters,
        sort: SortOption
    ): List<Vehicle> {
        var result = variants

        // Apply filters (Transmission, Car Type, Fuel Type, Seats, Brand, Rating, Price Range)
        if (filters.carTypes.isNotEmpty()) {
            result = result.filter { vehicle ->
                filters.carTypes.any { it.equals(vehicle.carType, ignoreCase = true) }
            }
        }

        if (filters.transmissions.isNotEmpty()) {
            result = result.filter { vehicle ->
                filters.transmissions.any { it.equals(vehicle.transmission, ignoreCase = true) }
            }
        }

        if (filters.fuelTypes.isNotEmpty()) {
            result = result.filter { vehicle ->
                filters.fuelTypes.any { it.equals(vehicle.fuelType, ignoreCase = true) }
            }
        }

        if (filters.seats.isNotEmpty()) {
            result = result.filter { vehicle ->
                vehicle.seats in filters.seats
            }
        }

        if (filters.brands.isNotEmpty()) {
            result = result.filter { vehicle ->
                filters.brands.any { it.equals(vehicle.company, ignoreCase = true) }
            }
        }

        if (filters.ratings.isNotEmpty()) {
            result = result.filter { vehicle ->
                val ratingFloat = vehicle.rating?.toFloatOrNull() ?: 0f
                filters.ratings.any { ratingFloat >= it }
            }
        }

        if (filters.priceRange != null) {
            result = result.filter { vehicle ->
                vehicle.price in filters.priceRange
            }
        }

        // Apply sorting (Price Low/High, Rating High)
        result = when (sort) {
            SortOption.PRICE_LOW_TO_HIGH -> result.sortedBy { it.price }
            SortOption.PRICE_HIGH_TO_LOW -> result.sortedByDescending { it.price }
            SortOption.RATING_HIGH_TO_LOW -> result.sortedByDescending { it.rating?.toFloatOrNull() ?: 0f }
            SortOption.NONE -> result
        }

        return result
    }
}
