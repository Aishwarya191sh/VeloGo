package com.example.frontend.domain.repository

import com.example.frontend.data.remote.dto.SearchVehiclesRequest
import com.example.frontend.data.remote.dto.VariantsRequest
import com.example.frontend.data.remote.dto.VehicleModelResponse
import com.example.frontend.domain.model.Vehicle

interface SearchRepository {
    suspend fun getVehicleModels(): Result<List<VehicleModelResponse>>
    suspend fun searchUniqueModels(request: SearchVehiclesRequest): Result<List<Vehicle>>
    suspend fun getVehicleVariants(request: VariantsRequest): Result<List<Vehicle>>
    suspend fun getVehicleDetails(id: String): Result<Vehicle>
}
