package com.example.frontend.data.remote.api

import com.example.frontend.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AdminApi {

    @GET("api/admin/getVehicleModels")
    suspend fun getVehicleModels(): Response<List<VehicleModelResponse>>

    @GET("api/admin/fetchVendorVehilceRequests")
    suspend fun fetchVendorVehicleRequests(): Response<List<VehicleDto>>

    @POST("api/admin/approveVendorVehicleRequest")
    suspend fun approveVendorVehicleRequest(
        @Body request: ApproveRejectVehicleRequest
    ): Response<BaseResponse<String>>

    @POST("api/admin/rejectVendorVehicleRequest")
    suspend fun rejectVendorVehicleRequest(
        @Body request: ApproveRejectVehicleRequest
    ): Response<BaseResponse<String>>

    @GET("api/admin/showVehicles")
    suspend fun showVehicles(): Response<List<VehicleDto>>

    @DELETE("api/admin/deleteVehicle/{id}")
    suspend fun deleteVehicle(
        @Path("id") id: String
    ): Response<BaseResponse<String>>

    @GET("api/admin/allBookings")
    suspend fun getAllBookings(): Response<List<VendorBookingResponseItem>>

    @POST("api/admin/changeStatus")
    suspend fun changeBookingStatus(
        @Body request: ChangeBookingStatusRequest
    ): Response<BaseResponse<String>>
}


