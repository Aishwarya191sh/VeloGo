package com.example.frontend.domain.model

data class Vehicle(
    val id: String,
    val registrationNumber: String,
    val carTitle: String?,
    val carDescription: String?,
    val company: String?,
    val name: String?,
    val model: String?,
    val yearMade: Int?,
    val fuelType: String?,
    val rentedBy: String?,
    val rating: String?,
    val seats: Int?,
    val transmission: String?, // "manual" or "automatic"
    val image: List<String>,
    val description: String?,
    val title: String?,
    val price: Double,
    val basePackage: String?,
    val withOrWithoutFuel: Boolean?,
    val carType: String?,
    val isDeleted: String?,
    val location: String,
    val district: String,
    val isBooked: Boolean,
    val isAdminApproved: Boolean,
    val isRejected: Boolean = false,
    val insuranceEnd: String? = null,
    val registrationEnd: String? = null,
    val pollutionEnd: String? = null
)

data class SearchFilters(
    val carTypes: Set<String> = emptySet(),
    val transmissions: Set<String> = emptySet(),
    val priceRange: ClosedRange<Double>? = null,
    val fuelTypes: Set<String> = emptySet(),
    val seats: Set<Int> = emptySet(),
    val brands: Set<String> = emptySet(),
    val ratings: Set<Float> = emptySet()
)

enum class SortOption {
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    RATING_HIGH_TO_LOW,
    NONE
}
