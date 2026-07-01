package com.example.frontend.data.remote.dto

data class ApproveRejectVehicleRequest(
    val _id: String
)

data class ChangeBookingStatusRequest(
    val id: String,
    val status: String
)
