package com.example.frontend.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("_id") val id: String,
    val username: String,
    val email: String,
    val phoneNumber: String? = null,
    val adress: String? = null, // Retain backend spelling 'adress'
    val profilePicture: String? = null,
    val isUser: Boolean = false,
    val isAdmin: Boolean = false,
    val isVendor: Boolean = false,
    val refreshToken: String? = null,
    val accessToken: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)
