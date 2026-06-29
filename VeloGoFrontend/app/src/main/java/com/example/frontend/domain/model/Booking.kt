package com.example.frontend.domain.model

data class Booking(
    val id: String,
    val pickupDate: String,
    val dropOffDate: String,
    val userId: String,
    val pickUpLocation: String,
    val vehicleId: String,
    val dropOffLocation: String,
    val pickUpDistrict: String,
    val totalPrice: Double,
    val razorpayPaymentId: String,
    val razorpayOrderId: String,
    val status: String,
    val createdAt: String?
)

data class BookingDetails(
    val booking: Booking,
    val vehicle: Vehicle
)
