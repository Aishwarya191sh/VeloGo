package com.example.frontend.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.domain.model.User
import com.example.frontend.domain.usecase.EditProfileUseCase
import com.example.frontend.domain.usecase.GetSessionUseCase
import com.example.frontend.ui.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getSessionUseCase: GetSessionUseCase,
    private val editProfileUseCase: EditProfileUseCase
) : ViewModel() {

    val currentUser: StateFlow<User?> = getSessionUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _profileUiState = MutableStateFlow(ProfileUiState())
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState.asStateFlow()

    fun editProfile(userId: String, username: String, email: String, phoneNumber: String, adress: String) {
        viewModelScope.launch {
            editProfileUseCase(userId, username, email, phoneNumber, adress).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _profileUiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }
                    }
                    is Resource.Success -> {
                        _profileUiState.update { it.copy(isLoading = false, error = null, isSuccess = true) }
                    }
                    is Resource.Error -> {
                        _profileUiState.update { it.copy(isLoading = false, error = resource.message, isSuccess = false) }
                    }
                }
            }
        }
    }

    fun resetUpdateState() {
        _profileUiState.update { it.copy(isLoading = false, error = null, isSuccess = false) }
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
