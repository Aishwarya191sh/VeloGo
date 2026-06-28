package com.example.frontend.data.mapper

import com.example.frontend.data.remote.dto.UserResponse
import com.example.frontend.domain.model.User

fun UserResponse.toDomain(): User {
    return User(
        id = id,
        username = username,
        email = email,
        phoneNumber = phoneNumber,
        adress = adress,
        profilePicture = profilePicture,
        isUser = isUser,
        isAdmin = isAdmin,
        isVendor = isVendor,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
