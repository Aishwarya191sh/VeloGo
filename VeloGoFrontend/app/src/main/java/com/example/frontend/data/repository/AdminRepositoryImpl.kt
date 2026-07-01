package com.example.frontend.data.repository

import com.example.frontend.data.mapper.toDomain
import com.example.frontend.data.remote.api.AdminApi
import com.example.frontend.data.remote.dto.*
import com.example.frontend.domain.model.Vehicle
import com.example.frontend.domain.model.VendorBooking
import com.example.frontend.domain.repository.AdminRepository
import com.example.frontend.ui.util.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val adminApi: AdminApi
) : AdminRepository {

    private val gson = Gson()

    override fun fetchVendorVehicleRequests(): Flow<Resource<List<Vehicle>>> = flow {
        emit(Resource.Loading)
        try {
            val response = adminApi.fetchVendorVehicleRequests()
            if (response.isSuccessful && response.body() != null) {
                val vehicles = response.body()!!.map { it.toDomain() }
                emit(Resource.Success(vehicles))
            } else {
                val errorMsg = parseError(response.errorBody()?.string())
                emit(Resource.Error(errorMsg))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your internet connection.", e))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred", e))
        }
    }

    override fun approveVendorVehicleRequest(id: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val response = adminApi.approveVendorVehicleRequest(ApproveRejectVehicleRequest(id))
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.message ?: "Vehicle request approved successfully"))
            } else {
                val errorMsg = parseError(response.errorBody()?.string())
                emit(Resource.Error(errorMsg))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your internet connection.", e))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred", e))
        }
    }

    override fun rejectVendorVehicleRequest(id: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val response = adminApi.rejectVendorVehicleRequest(ApproveRejectVehicleRequest(id))
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.message ?: "Vehicle request rejected successfully"))
            } else {
                val errorMsg = parseError(response.errorBody()?.string())
                emit(Resource.Error(errorMsg))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your internet connection.", e))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred", e))
        }
    }

    override fun getShowVehicles(): Flow<Resource<List<Vehicle>>> = flow {
        emit(Resource.Loading)
        try {
            val response = adminApi.showVehicles()
            if (response.isSuccessful && response.body() != null) {
                val vehicles = response.body()!!.map { it.toDomain() }
                emit(Resource.Success(vehicles))
            } else {
                val errorMsg = parseError(response.errorBody()?.string())
                emit(Resource.Error(errorMsg))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your internet connection.", e))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred", e))
        }
    }

    override fun deleteVehicle(id: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val response = adminApi.deleteVehicle(id)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.message ?: "Vehicle deleted successfully"))
            } else {
                val errorMsg = parseError(response.errorBody()?.string())
                emit(Resource.Error(errorMsg))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your internet connection.", e))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred", e))
        }
    }

    override fun getAllBookings(): Flow<Resource<List<VendorBooking>>> = flow {
        emit(Resource.Loading)
        try {
            val response = adminApi.getAllBookings()
            if (response.isSuccessful && response.body() != null) {
                val bookings = response.body()!!.map { item ->
                    VendorBooking(
                        id = item._id,
                        vehicleId = item.vehicleId,
                        pickupDate = item.pickupDate,
                        dropOffDate = item.dropOffDate,
                        userId = item.userId,
                        pickUpLocation = item.pickUpLocation,
                        dropOffLocation = item.dropOffLocation,
                        totalPrice = item.totalPrice,
                        razorpayOrderId = item.razorpayOrderId,
                        razorpayPaymentId = item.razorpayPaymentId,
                        status = item.status,
                        createdAt = item.createdAt,
                        vehicleDetails = item.vehicleDetails.toDomain()
                    )
                }
                emit(Resource.Success(bookings))
            } else {
                val errorMsg = parseError(response.errorBody()?.string())
                emit(Resource.Error(errorMsg))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your internet connection.", e))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred", e))
        }
    }

    override fun changeBookingStatus(id: String, status: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val response = adminApi.changeBookingStatus(ChangeBookingStatusRequest(id, status))
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.message ?: "Booking status changed successfully"))
            } else {
                val errorMsg = parseError(response.errorBody()?.string())
                emit(Resource.Error(errorMsg))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your internet connection.", e))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred", e))
        }
    }

    private fun parseError(errorJson: String?): String {
        if (errorJson.isNullOrEmpty()) return "An unknown error occurred"
        return try {
            val baseResponse = gson.fromJson(errorJson, BaseResponse::class.java)
            baseResponse.message ?: "An error occurred"
        } catch (e: Exception) {
            "An error occurred"
        }
    }
}
