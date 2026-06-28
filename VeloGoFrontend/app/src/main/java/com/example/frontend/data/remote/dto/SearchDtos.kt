package com.example.frontend.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VehicleModelResponse(
    val _id: String,
    val type: String, // "car" or "location"
    val model: String?,
    val brand: String?,
    val location: String?,
    val district: String?
)

data class SearchVehiclesRequest(
    val pickupDate: String,
    val dropOffDate: String,
    val pickUpDistrict: String,
    val pickUpLocation: String
)

data class VehicleDto(
    val _id: String,
    val registeration_number: String?,
    val car_title: String?,
    val car_description: String?,
    val company: String?,
    val name: String?,
    val model: String?,
    val year_made: Int?,
    val fuel_type: String?,
    val rented_by: String?,
    val rating: List<String>?,
    val seats: Int?,
    val transmition: String?,
    val image: List<String>?,
    val description: String?,
    val title: String?,
    val price: Double?,
    val base_package: String?,
    val with_or_without_fuel: Boolean?,
    val car_type: String?,
    val isDeleted: String?,
    val location: String?,
    val district: String?,
    @SerializedName("isBooked")
    val isBooked: Boolean?,
    @SerializedName("isAdminApproved")
    val isAdminApproved: Boolean?,
    val isRejected: Boolean? = null,
    val insurance_end: String? = null,
    val registeration_end: String? = null,
    val pollution_end: String? = null
)

data class VariantsRequest(
    val pickUpDistrict: String,
    val pickUpLocation: String,
    val pickupDate: String,
    val dropOffDate: String,
    val model: String
)

data class VehicleDetailsRequest(
    val id: String
)

