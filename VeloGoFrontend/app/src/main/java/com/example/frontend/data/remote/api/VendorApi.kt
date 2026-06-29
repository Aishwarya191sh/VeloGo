package com.example.frontend.data.remote.api

import com.example.frontend.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface VendorApi {

    @POST("api/vendor/showVendorVehilces")
    suspend fun showVendorVehicles(
        @Body request: VendorVehiclesRequest
    ): Response<List<VehicleDto>>

    @Multipart
    @POST("api/vendor/vendorAddVehicle")
    suspend fun addVehicle(
        @Part("registeration_number") registerationNumber: RequestBody,
        @Part("company") company: RequestBody,
        @Part("name") name: RequestBody,
        @Part("model") model: RequestBody,
        @Part("title") title: RequestBody,
        @Part("base_package") basePackage: RequestBody,
        @Part("price") price: RequestBody,
        @Part("year_made") yearMade: RequestBody,
        @Part("fuel_type") fuelType: RequestBody,
        @Part("description") description: RequestBody,
        @Part("seat") seat: RequestBody,
        @Part("transmition_type") transmitionType: RequestBody,
        @Part("insurance_end_date") insuranceEndDate: RequestBody,
        @Part("registeration_end_date") registerationEndDate: RequestBody,
        @Part("polution_end_date") pollutionEndDate: RequestBody,
        @Part("car_type") carType: RequestBody,
        @Part("location") location: RequestBody,
        @Part("district") district: RequestBody,
        @Part("addedBy") addedBy: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<VendorAddVehicleResponse>

    @PUT("api/vendor/vendorEditVehicles/{id}")
    suspend fun editVehicle(
        @Path("id") id: String,
        @Body request: EditVehicleRequest
    ): Response<VehicleDto>

    @DELETE("api/vendor/vendorDeleteVehicles/{id}")
    suspend fun deleteVehicle(
        @Path("id") id: String
    ): Response<VendorDeleteResponse>

    @POST("api/vendor/vendorBookings")
    suspend fun getVendorBookings(
        @Body request: VendorBookingsRequest
    ): Response<List<VendorBookingResponseItem>>

    @POST("api/admin/changeStatus")
    suspend fun changeBookingStatus(
        @Body request: ChangeBookingStatusRequest
    ): Response<BaseResponse<String>>
}
