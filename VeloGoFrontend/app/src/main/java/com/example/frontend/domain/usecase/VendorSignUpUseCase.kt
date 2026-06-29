package com.example.frontend.domain.usecase

import com.example.frontend.domain.repository.AuthRepository
import com.example.frontend.ui.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VendorSignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(username: String, email: String, password: String): Flow<Resource<String>> {
        return repository.vendorSignUp(username, email, password)
    }
}
