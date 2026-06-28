 package com.example.frontend.data.mapper

import com.example.frontend.data.remote.dto.VehicleDto
import com.example.frontend.domain.model.Vehicle

fun VehicleDto.toDomain(): Vehicle {
    return Vehicle(
        id = _id,
        registrationNumber = registeration_number ?: "",
        carTitle = car_title,
        carDescription = car_description,
        company = company,
        name = name,
        model = model,
        yearMade = year_made,
        fuelType = fuel_type,
        rentedBy = rented_by,
        rating = rating?.let { list ->
            val valid = list.mapNotNull { it.trim().toFloatOrNull() }
            if (valid.isEmpty()) null else valid.average().toString()
        },
        seats = seats,
        transmission = transmition,
        image = image ?: emptyList(),
        description = description,
        title = title,
        price = price ?: 0.0,
        basePackage = base_package,
        withOrWithoutFuel = with_or_without_fuel,
        carType = car_type,
        isDeleted = isDeleted,
        location = location ?: "",
        district = district ?: "",
        isBooked = isBooked ?: false,
        isAdminApproved = isAdminApproved ?: false,
        isRejected = isRejected ?: false,
        insuranceEnd = insurance_end,
        registrationEnd = registeration_end,
        pollutionEnd = pollution_end
    )
}
