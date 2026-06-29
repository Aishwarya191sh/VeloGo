package com.example.frontend.domain.model

data class VendorBooking(
    val id: String,
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
    val vehicleDetails: Vehicle
)
