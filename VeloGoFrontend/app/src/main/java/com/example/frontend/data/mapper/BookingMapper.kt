package com.example.frontend.data.mapper

import com.example.frontend.data.remote.dto.BookingDto
import com.example.frontend.data.remote.dto.BookingResponseItem
import com.example.frontend.domain.model.Booking
import com.example.frontend.domain.model.BookingDetails

fun BookingDto.toDomain(): Booking {
    return Booking(
        id = _id,
        pickupDate = pickupDate ?: "",
        dropOffDate = dropOffDate ?: "",
        userId = userId ?: "",
        pickUpLocation = pickUpLocation ?: "",
        vehicleId = vehicleId ?: "",
        dropOffLocation = dropOffLocation ?: "",
        pickUpDistrict = pickUpDistrict ?: "",
        totalPrice = totalPrice ?: 0.0,
        razorpayPaymentId = razorpayPaymentId ?: "",
        razorpayOrderId = razorpayOrderId ?: "",
        status = status ?: "",
        createdAt = createdAt
    )
}

fun BookingResponseItem.toDomain(): BookingDetails {
    return BookingDetails(
        booking = bookingDetails.toDomain(),
        vehicle = vehicleDetails.toDomain()
    )
}
