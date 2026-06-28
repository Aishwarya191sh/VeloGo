package com.example.frontend.domain.usecase

import com.example.frontend.domain.repository.AuthRepository
import com.example.frontend.ui.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<Resource<Unit>> {
        return repository.signOut()
    }
}
