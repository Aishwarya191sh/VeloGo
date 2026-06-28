package com.example.frontend.domain.model

data class User(
    val id: String,
    val username: String,
    val email: String,
    val phoneNumber: String?,
    val adress: String?, // Retain backend spelling 'adress'
    val profilePicture: String?,
    val isUser: Boolean,
    val isAdmin: Boolean,
    val isVendor: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)
