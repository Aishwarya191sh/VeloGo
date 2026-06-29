package com.example.frontend.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VendorVehiclesRequest(
    val _id: String
)

data class VendorBookingsRequest(
    val vendorVehicles: List<String>
)

data class EditVehicleRequest(
    val formData: EditVehicleFormData
)

data class EditVehicleFormData(
    val registeration_number: String,
    val company: String,
    val name: String,
    val model: String,
    val title: String,
    val base_package: String,
    val price: Double,
    val year_made: Int,
    val description: String,
    val Seats: Int,
    val transmitionType: String,
    val Registeration_end_date: String,
    val insurance_end_date: String,
    val polution_end_date: String,
    val carType: String,
    val fuelType: String,
    val vehicleLocation: String,
    val vehicleDistrict: String
)

data class VendorAddVehicleResponse(
    val message: String?
)

data class VendorDeleteResponse(
    val message: String?
)

data class VendorBookingResponseItem(
    val _id: String,
    val vehicleId: String,
    val pickupDate: String,
    val dropOffDate: String,
    val userId: String?,
    val pickUpLocation: String,
    val dropOffLocation: String,
    val totalPrice: Double,
    val razorpayOrderId: String?,
    val razorpayPaymentId: String?,
    val status: String,
    val createdAt: String?,
    val vehicleDetails: VehicleDto
)

data class ChangeBookingStatusRequest(
    val id: String,
    val status: String
)
