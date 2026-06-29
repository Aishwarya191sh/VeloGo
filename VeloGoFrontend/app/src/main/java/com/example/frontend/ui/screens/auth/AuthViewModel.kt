package com.example.frontend.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.domain.usecase.*
import com.example.frontend.ui.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getSessionUseCase: GetSessionUseCase,
    private val vendorSignInUseCase: VendorSignInUseCase,
    private val vendorSignUpUseCase: VendorSignUpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentSession = MutableStateFlow<com.example.frontend.domain.model.User?>(null)
    val currentSession: StateFlow<com.example.frontend.domain.model.User?> = _currentSession.asStateFlow()

    init {
        viewModelScope.launch {
            getSessionUseCase().collect { session ->
                _currentSession.value = session
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            signInUseCase(email, password).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, user = resource.data, error = null) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = resource.message) }
                }
            }
        }
    }

    fun signUp(username: String, email: String, password: String) {
        viewModelScope.launch {
            signUpUseCase(username, email, password).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, error = null, signupSuccessMessage = null) }
                    is Resource.Success -> {
                        _uiState.update { it.copy(signupSuccessMessage = "Account created successfully! Logging you in...") }
                        signIn(email, password)
                    }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = resource.message) }
                }
            }
        }
    }

    fun signInWithGoogle(name: String, email: String, photo: String, isVendor: Boolean) {
        viewModelScope.launch {
            googleSignInUseCase(name, email, photo, isVendor).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, user = resource.data, error = null) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = resource.message) }
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase().collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.update { AuthUiState() }
                }
            }
        }
    }

    fun vendorSignIn(email: String, password: String) {
        viewModelScope.launch {
            vendorSignInUseCase(email, password).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, user = resource.data, error = null) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = resource.message) }
                }
            }
        }
    }

    fun vendorSignUp(username: String, email: String, password: String) {
        viewModelScope.launch {
            vendorSignUpUseCase(username, email, password).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, error = null, signupSuccessMessage = null) }
                    is Resource.Success -> {
                        _uiState.update { it.copy(signupSuccessMessage = "Vendor account created successfully! Logging you in...") }
                        vendorSignIn(email, password)
                    }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = resource.message) }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(signupSuccessMessage = null) }
    }
}
