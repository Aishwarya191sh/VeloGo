package com.example.frontend.data.remote.dto

data class SignInRequest(
    val email: String,
    val password: String
)

data class SignUpRequest(
    val username: String,
    val email: String,
    val password: String
)

data class GoogleAuthRequest(
    val name: String,
    val email: String,
    val photo: String
)

data class EditProfileRequest(
    val formData: EditProfileFormData
)

data class EditProfileFormData(
    val username: String,
    val email: String,
    val phoneNumber: String,
    val adress: String
)
