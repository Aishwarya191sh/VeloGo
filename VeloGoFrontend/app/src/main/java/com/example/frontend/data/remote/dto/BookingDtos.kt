package com.example.frontend.data.remote.dto

data class RazorpayOrderRequest(
    val totalPrice: Double,
    val dropoff_location: String,
    val pickup_district: String,
    val pickup_location: String
)

data class RazorpayOrderResponse(
    val id: String,
    val amount: Int,
    val currency: String
)

data class BookCarRequest(
    val user_id: String,
    val vehicle_id: String,
    val totalPrice: Double,
    val pickupDate: String,
    val dropoffDate: String,
    val pickup_location: String,
    val dropoff_location: String,
    val pickup_district: String,
    val razorpayPaymentId: String,
    val razorpayOrderId: String
)

data class BookCarResponse(
    val message: String,
    val booked: BookingDto
)

data class BookingDto(
    val _id: String,
    val pickupDate: String?,
    val dropOffDate: String?,
    val userId: String?,
    val pickUpLocation: String?,
    val vehicleId: String?,
    val dropOffLocation: String?,
    val pickUpDistrict: String?,
    val totalPrice: Double?,
    val razorpayPaymentId: String?,
    val razorpayOrderId: String?,
    val status: String?,
    val createdAt: String?
)

data class LatestBookingsRequest(
    val user_id: String
)

data class BookingResponseItem(
    val bookingDetails: BookingDto,
    val vehicleDetails: VehicleDto
)

data class SendBookingEmailRequest(
    val toEmail: String,
    val data: List<BookingResponseItem>
)

data class UserBookingsRequest(
    val userId: String
)
