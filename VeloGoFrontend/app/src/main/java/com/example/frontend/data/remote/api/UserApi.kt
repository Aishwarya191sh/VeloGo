package com.example.frontend.data.remote.api

import com.example.frontend.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApi {

    @POST("api/user/showSingleofSameModel")
    suspend fun searchUniqueModels(
        @Body request: SearchVehiclesRequest
    ): Response<List<VehicleDto>>

    @POST("api/user/getVehiclesWithoutBooking")
    suspend fun getVehicleVariants(
        @Body request: VariantsRequest
    ): Response<List<VehicleDto>>

    @POST("api/user/showVehicleDetails")
    suspend fun getVehicleDetails(
        @Body request: VehicleDetailsRequest
    ): Response<VehicleDto>

    @POST("api/user/razorpay")
    suspend fun createRazorpayOrder(
        @Body request: RazorpayOrderRequest
    ): Response<RazorpayOrderResponse>

    @POST("api/user/bookCar")
    suspend fun bookCar(
        @Body request: BookCarRequest
    ): Response<BookCarResponse>

    @POST("api/user/latestbookings")
    suspend fun getLatestBooking(
        @Body request: LatestBookingsRequest
    ): Response<List<BookingResponseItem>>

    @POST("api/user/sendBookingDetailsEamil")
    suspend fun sendBookingDetailsEmail(
        @Body request: SendBookingEmailRequest
    ): Response<String>

    @POST("api/user/findBookingsOfUser")
    suspend fun findBookingsOfUser(
        @Body request: UserBookingsRequest
    ): Response<List<BookingResponseItem>>

    @POST("api/user/editUserProfile/{id}")
    suspend fun editUserProfile(
        @Path("id") id: String,
        @Body request: EditProfileRequest
    ): Response<UserResponse>
}
