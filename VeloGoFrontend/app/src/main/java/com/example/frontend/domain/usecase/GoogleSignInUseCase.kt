package com.example.frontend.domain.usecase

import com.example.frontend.domain.model.User
import com.example.frontend.domain.repository.AuthRepository
import com.example.frontend.ui.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(name: String, email: String, photo: String, isVendor: Boolean): Flow<Resource<User>> {
        return repository.signInWithGoogle(name, email, photo, isVendor)
    }
}
