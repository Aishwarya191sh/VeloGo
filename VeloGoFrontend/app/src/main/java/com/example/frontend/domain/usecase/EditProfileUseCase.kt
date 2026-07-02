package com.example.frontend.domain.usecase

import com.example.frontend.domain.model.User
import com.example.frontend.domain.repository.AuthRepository
import com.example.frontend.ui.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EditProfileUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(
        userId: String,
        username: String,
        email: String,
        phoneNumber: String,
        adress: String
    ): Flow<Resource<User>> {
        return repository.editUserProfile(userId, username, email, phoneNumber, adress)
    }
}
