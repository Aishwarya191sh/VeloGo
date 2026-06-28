package com.example.frontend.domain.usecase

import com.example.frontend.domain.model.User
import com.example.frontend.domain.repository.AuthRepository
import com.example.frontend.ui.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(email: String, password: String): Flow<Resource<User>> {
        return repository.signIn(email, password)
    }
}
