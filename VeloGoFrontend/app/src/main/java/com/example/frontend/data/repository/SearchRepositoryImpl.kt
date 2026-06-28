package com.example.frontend.data.repository

import com.example.frontend.data.mapper.toDomain
import com.example.frontend.data.remote.api.AdminApi
import com.example.frontend.data.remote.api.UserApi
import com.example.frontend.data.remote.dto.SearchVehiclesRequest
import com.example.frontend.data.remote.dto.VariantsRequest
import com.example.frontend.data.remote.dto.VehicleDetailsRequest
import com.example.frontend.data.remote.dto.VehicleModelResponse
import com.example.frontend.domain.model.Vehicle
import com.example.frontend.domain.repository.SearchRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val adminApi: AdminApi
) : SearchRepository {

    override suspend fun getVehicleModels(): Result<List<VehicleModelResponse>> {
        return try {
            val response = adminApi.getVehicleModels()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Failed to load locations metadata" }))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error. Please check your internet connection.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUniqueModels(request: SearchVehiclesRequest): Result<List<Vehicle>> {
        return try {
            val response = userApi.searchUniqueModels(request)
            if (response.isSuccessful && response.body() != null) {
                val vehicles = response.body()!!.map { it.toDomain() }
                Result.success(vehicles)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Failed to search vehicles" }))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error. Please check your internet connection.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVehicleVariants(request: VariantsRequest): Result<List<Vehicle>> {
        return try {
            val response = userApi.getVehicleVariants(request)
            if (response.isSuccessful && response.body() != null) {
                val variants = response.body()!!.map { it.toDomain() }
                Result.success(variants)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Failed to fetch vehicle variants" }))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error. Please check your internet connection.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVehicleDetails(id: String): Result<Vehicle> {
        return try {
            val response = userApi.getVehicleDetails(VehicleDetailsRequest(id))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Failed to load vehicle details" }))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error. Please check your internet connection.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
