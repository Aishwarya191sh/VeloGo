package com.example.frontend.domain.repository

import com.example.frontend.data.remote.dto.RazorpayOrderResponse
import com.example.frontend.domain.model.Booking
import com.example.frontend.domain.model.BookingDetails

interface BookingRepository {
    suspend fun createRazorpayOrder(
        totalPrice: Double,
        dropoffLocation: String,
        pickupDistrict: String,
        pickupLocation: String
    ): Result<RazorpayOrderResponse>

    suspend fun bookCar(
        userId: String,
        vehicleId: String,
        totalPrice: Double,
        pickupDate: String,
        dropoffDate: String,
        pickupLocation: String,
        dropoffLocation: String,
        pickupDistrict: String,
        razorpayPaymentId: String,
        razorpayOrderId: String
    ): Result<Booking>

    suspend fun getLatestBooking(userId: String): Result<BookingDetails?>

    suspend fun findBookingsOfUser(userId: String): Result<List<BookingDetails>>

    suspend fun sendBookingEmail(toEmail: String, bookingDetails: BookingDetails): Result<Boolean>
}
