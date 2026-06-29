package com.example.frontend.data.repository

import com.example.frontend.data.mapper.toDomain
import com.example.frontend.data.remote.api.VendorApi
import com.example.frontend.data.remote.dto.*
import com.example.frontend.domain.model.Vehicle
import com.example.frontend.domain.model.VendorBooking
import com.example.frontend.domain.repository.VendorRepository
import com.example.frontend.ui.util.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorRepositoryImpl @Inject constructor(
    private val vendorApi: VendorApi
) : VendorRepository {

    private val gson = Gson()

    override fun getVendorVehicles(vendorId: String): Flow<Resource<List<Vehicle>>> = flow {
        emit(Resource.Loading)
        try {
            val response = vendorApi.showVendorVehicles(VendorVehiclesRequest(vendorId))
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

    override fun addVehicle(
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
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            // Helper to convert String to plain RequestBody
            fun String.toTextPart(): RequestBody {
                return this.toRequestBody("text/plain".toMediaTypeOrNull())
            }

            val regPart = registerationNumber.toTextPart()
            val compPart = company.toTextPart()
            val namePart = name.toTextPart()
            val modelPart = model.toTextPart()
            val titlePart = title.toTextPart()
            val basePart = basePackage.toTextPart()
            val pricePart = price.toString().toTextPart()
            val yearPart = yearMade.toString().toTextPart()
            val fuelPart = fuelType.toTextPart()
            val descPart = description.toTextPart()
            val seatPart = seat.toString().toTextPart()
            val transPart = transmitionType.toTextPart()
            val insPart = insuranceEndDate.toTextPart()
            val regEndPart = registrationEndDate.toTextPart()
            val polPart = pollutionEndDate.toTextPart()
            val carPart = carType.toTextPart()
            val locPart = location.toTextPart()
            val distPart = district.toTextPart()
            val addedByPart = addedBy.toTextPart()

            // Map image byte arrays to MultipartBody.Part
            val imageParts = images.map { (filename, bytes) ->
                val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", filename, requestFile)
            }

            val response = vendorApi.addVehicle(
                registerationNumber = regPart,
                company = compPart,
                name = namePart,
                model = modelPart,
                title = titlePart,
                basePackage = basePart,
                price = pricePart,
                yearMade = yearPart,
                fuelType = fuelPart,
                description = descPart,
                seat = seatPart,
                transmitionType = transPart,
                insuranceEndDate = insPart,
                registerationEndDate = regEndPart,
                pollutionEndDate = polPart,
                carType = carPart,
                location = locPart,
                district = distPart,
                addedBy = addedByPart,
                images = imageParts
            )

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.message ?: "Vehicle add request submitted successfully"))
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

    override fun editVehicle(
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
    ): Flow<Resource<Vehicle>> = flow {
        emit(Resource.Loading)
        try {
            val formData = EditVehicleFormData(
                registeration_number = registerationNumber,
                company = company,
                name = name,
                model = model,
                title = title,
                base_package = basePackage,
                price = price,
                year_made = yearMade,
                description = description,
                Seats = seats,
                transmitionType = transmitionType,
                Registeration_end_date = registrationEndDate,
                insurance_end_date = insuranceEndDate,
                polution_end_date = pollutionEndDate,
                carType = carType,
                fuelType = fuelType,
                vehicleLocation = location,
                vehicleDistrict = district
            )
            val request = EditVehicleRequest(formData)
            val response = vendorApi.editVehicle(vehicleId, request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.toDomain()))
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

    override fun deleteVehicle(vehicleId: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val response = vendorApi.deleteVehicle(vehicleId)
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

    override fun getVendorBookings(vehicleIds: List<String>): Flow<Resource<List<VendorBooking>>> = flow {
        emit(Resource.Loading)
        try {
            val response = vendorApi.getVendorBookings(VendorBookingsRequest(vehicleIds))
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

    override fun changeBookingStatus(bookingId: String, status: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val response = vendorApi.changeBookingStatus(ChangeBookingStatusRequest(bookingId, status))
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
