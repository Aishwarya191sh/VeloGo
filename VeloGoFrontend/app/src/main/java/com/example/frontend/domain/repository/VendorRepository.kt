package com.example.frontend.domain.repository

import com.example.frontend.domain.model.Vehicle
import com.example.frontend.domain.model.VendorBooking
import com.example.frontend.ui.util.Resource
import kotlinx.coroutines.flow.Flow

interface VendorRepository {
    
    fun getVendorVehicles(vendorId: String): Flow<Resource<List<Vehicle>>>
    
    fun addVehicle(
        registerationNumber: String,
        company: String,
        name: String,
        model: String,
        title: String,
        basePackage: String,
        price: Double,
        yearMade: Int,
        fuelType: String,
        description: String,
        seat: Int,
        transmitionType: String,
        insuranceEndDate: String,
        registrationEndDate: String,
        pollutionEndDate: String,
        carType: String,
        location: String,
        district: String,
        addedBy: String,
        images: List<Pair<String, ByteArray>>
    ): Flow<Resource<String>>

    fun editVehicle(
        vehicleId: String,
        registerationNumber: String,
        company: String,
        name: String,
        model: String,
        title: String,
        basePackage: String,
        price: Double,
        yearMade: Int,
        description: String,
        seats: Int,
        transmitionType: String,
        registrationEndDate: String,
        insuranceEndDate: String,
        pollutionEndDate: String,
        carType: String,
        fuelType: String,
        location: String,
        district: String
    ): Flow<Resource<Vehicle>>

    fun deleteVehicle(vehicleId: String): Flow<Resource<String>>

    fun getVendorBookings(vehicleIds: List<String>): Flow<Resource<List<VendorBooking>>>

    fun changeBookingStatus(bookingId: String, status: String): Flow<Resource<String>>
}
