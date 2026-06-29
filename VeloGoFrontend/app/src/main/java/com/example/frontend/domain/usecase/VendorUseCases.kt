package com.example.frontend.domain.usecase

import com.example.frontend.domain.model.Vehicle
import com.example.frontend.domain.model.VendorBooking
import com.example.frontend.domain.repository.VendorRepository
import com.example.frontend.ui.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVendorVehiclesUseCase @Inject constructor(
    private val repository: VendorRepository
) {
    operator fun invoke(vendorId: String): Flow<Resource<List<Vehicle>>> {
        return repository.getVendorVehicles(vendorId)
    }
}

class AddVehicleUseCase @Inject constructor(
    private val repository: VendorRepository
) {
    operator fun invoke(
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
    ): Flow<Resource<String>> {
        return repository.addVehicle(
            registerationNumber, company, name, model, title, basePackage, price, yearMade,
            fuelType, description, seat, transmitionType, insuranceEndDate, registrationEndDate,
            pollutionEndDate, carType, location, district, addedBy, images
        )
    }
}

class EditVehicleUseCase @Inject constructor(
    private val repository: VendorRepository
) {
    operator fun invoke(
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
    ): Flow<Resource<Vehicle>> {
        return repository.editVehicle(
            vehicleId, registerationNumber, company, name, model, title, basePackage, price, yearMade,
            description, seats, transmitionType, registrationEndDate, insuranceEndDate, pollutionEndDate,
            carType, fuelType, location, district
        )
    }
}

class DeleteVehicleUseCase @Inject constructor(
    private val repository: VendorRepository
) {
    operator fun invoke(vehicleId: String): Flow<Resource<String>> {
        return repository.deleteVehicle(vehicleId)
    }
}

class GetVendorBookingsUseCase @Inject constructor(
    private val repository: VendorRepository
) {
    operator fun invoke(vehicleIds: List<String>): Flow<Resource<List<VendorBooking>>> {
        return repository.getVendorBookings(vehicleIds)
    }
}
