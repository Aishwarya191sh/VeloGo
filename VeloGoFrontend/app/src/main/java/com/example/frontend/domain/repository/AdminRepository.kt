package com.example.frontend.domain.repository

import com.example.frontend.domain.model.Vehicle
import com.example.frontend.domain.model.VendorBooking
import com.example.frontend.ui.util.Resource
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    fun fetchVendorVehicleRequests(): Flow<Resource<List<Vehicle>>>
    fun approveVendorVehicleRequest(id: String): Flow<Resource<String>>
    fun rejectVendorVehicleRequest(id: String): Flow<Resource<String>>
    fun getShowVehicles(): Flow<Resource<List<Vehicle>>>
    fun deleteVehicle(id: String): Flow<Resource<String>>
    fun getAllBookings(): Flow<Resource<List<VendorBooking>>>
    fun changeBookingStatus(id: String, status: String): Flow<Resource<String>>
}
