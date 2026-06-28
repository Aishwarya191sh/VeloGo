package com.example.frontend.domain.usecase

import com.example.frontend.domain.model.User
import com.example.frontend.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSessionUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<User?> {
        return repository.getSessionDetails()
    }
}
