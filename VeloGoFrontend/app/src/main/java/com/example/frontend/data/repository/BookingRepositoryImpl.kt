package com.example.frontend.data.repository

import com.example.frontend.data.mapper.toDomain
import com.example.frontend.data.remote.api.UserApi
import com.example.frontend.data.remote.dto.*
import com.example.frontend.domain.model.Booking
import com.example.frontend.domain.model.BookingDetails
import com.example.frontend.domain.repository.BookingRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val userApi: UserApi
) : BookingRepository {

    override suspend fun createRazorpayOrder(
        totalPrice: Double,
        dropoffLocation: String,
        pickupDistrict: String,
        pickupLocation: String
    ): Result<RazorpayOrderResponse> {
        return try {
            val response = userApi.createRazorpayOrder(
                RazorpayOrderRequest(totalPrice, dropoffLocation, pickupDistrict, pickupLocation)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Failed to create payment order" }))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error. Please check your internet connection.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun bookCar(
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
    ): Result<Booking> {
        return try {
            val response = userApi.bookCar(
                BookCarRequest(
                    user_id = userId,
                    vehicle_id = vehicleId,
                    totalPrice = totalPrice,
                    pickupDate = pickupDate,
                    dropoffDate = dropoffDate,
                    pickup_location = pickupLocation,
                    dropoff_location = dropoffLocation,
                    pickup_district = pickupDistrict,
                    razorpayPaymentId = razorpayPaymentId,
                    razorpayOrderId = razorpayOrderId
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.booked.toDomain())
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Failed to record booking details" }))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error. Please check your internet connection.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLatestBooking(userId: String): Result<BookingDetails?> {
        return try {
            val response = userApi.getLatestBooking(LatestBookingsRequest(userId))
            if (response.isSuccessful && response.body() != null) {
                val item = response.body()!!.firstOrNull()?.toDomain()
                Result.success(item)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Failed to load booking details" }))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error. Please check your internet connection.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendBookingEmail(toEmail: String, bookingDetails: BookingDetails): Result<Boolean> {
        return try {
            val responseItem = BookingResponseItem(
                bookingDetails = BookingDto(
                    _id = bookingDetails.booking.id,
                    pickupDate = bookingDetails.booking.pickupDate,
                    dropOffDate = bookingDetails.booking.dropOffDate,
                    userId = bookingDetails.booking.userId,
                    pickUpLocation = bookingDetails.booking.pickUpLocation,
                    vehicleId = bookingDetails.booking.vehicleId,
                    dropOffLocation = bookingDetails.booking.dropOffLocation,
                    pickUpDistrict = bookingDetails.booking.pickUpDistrict,
                    totalPrice = bookingDetails.booking.totalPrice,
                    razorpayPaymentId = bookingDetails.booking.razorpayPaymentId,
                    razorpayOrderId = bookingDetails.booking.razorpayOrderId,
                    status = bookingDetails.booking.status,
                    createdAt = bookingDetails.booking.createdAt
                ),
                vehicleDetails = VehicleDto(
                    _id = bookingDetails.vehicle.id,
                    registeration_number = bookingDetails.vehicle.registrationNumber,
                    car_title = bookingDetails.vehicle.carTitle,
                    car_description = bookingDetails.vehicle.carDescription,
                    company = bookingDetails.vehicle.company,
                    name = bookingDetails.vehicle.name,
                    model = bookingDetails.vehicle.model,
                    year_made = bookingDetails.vehicle.yearMade,
                    fuel_type = bookingDetails.vehicle.fuelType,
                    rented_by = bookingDetails.vehicle.rentedBy,
                    rating = bookingDetails.vehicle.rating?.let { listOf(it) },
                    seats = bookingDetails.vehicle.seats,
                    transmition = bookingDetails.vehicle.transmission,
                    image = bookingDetails.vehicle.image,
                    description = bookingDetails.vehicle.description,
                    title = bookingDetails.vehicle.title,
                    price = bookingDetails.vehicle.price,
                    base_package = bookingDetails.vehicle.basePackage,
                    with_or_without_fuel = bookingDetails.vehicle.withOrWithoutFuel,
                    car_type = bookingDetails.vehicle.carType,
                    isDeleted = bookingDetails.vehicle.isDeleted,
                    location = bookingDetails.vehicle.location,
                    district = bookingDetails.vehicle.district,
                    isBooked = bookingDetails.vehicle.isBooked,
                    isAdminApproved = bookingDetails.vehicle.isAdminApproved
                )
            )
            val response = userApi.sendBookingDetailsEmail(
                SendBookingEmailRequest(toEmail, listOf(responseItem))
            )
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to send booking details email"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error. Please check your internet connection.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
