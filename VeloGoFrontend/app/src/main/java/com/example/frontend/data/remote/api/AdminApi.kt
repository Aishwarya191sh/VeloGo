package com.example.frontend.data.remote.api

import com.example.frontend.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AdminApi {

    @GET("api/admin/getVehicleModels")
    suspend fun getVehicleModels(): Response<List<VehicleModelResponse>>
}
