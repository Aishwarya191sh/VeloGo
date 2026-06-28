package com.example.frontend.ui.screens.auth

import com.example.frontend.domain.model.User

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val signupSuccessMessage: String? = null
)
