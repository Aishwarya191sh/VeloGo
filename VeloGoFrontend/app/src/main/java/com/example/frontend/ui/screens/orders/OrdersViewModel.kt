package com.example.frontend.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.domain.model.BookingDetails
import com.example.frontend.domain.repository.BookingRepository
import com.example.frontend.domain.usecase.GetSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrdersUiState(
    val isLoading: Boolean = false,
    val bookings: List<BookingDetails> = emptyList(),
    val errorMessage: String? = null,
    val isDetailModalOpen: Boolean = false,
    val selectedOrderDetails: BookingDetails? = null
)

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val getSessionUseCase: GetSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null

    init {
        loadUserSessionAndBookings()
    }

    private fun loadUserSessionAndBookings() {
        viewModelScope.launch {
            getSessionUseCase().collect { session ->
                if (session != null) {
                    currentUserId = session.id
                    fetchBookings(session.id)
                } else {
                    _uiState.update {
                        it.copy(
                            bookings = emptyList(),
                            errorMessage = "No active user session found"
                        )
                    }
                }
            }
        }
    }

    fun fetchBookings() {
        currentUserId?.let { userId ->
            fetchBookings(userId)
        }
    }

    private fun fetchBookings(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = bookingRepository.findBookingsOfUser(userId)
            result.onSuccess { list ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        bookings = list.sortedByDescending { bookingItem -> bookingItem.booking.createdAt ?: bookingItem.booking.pickupDate },
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to retrieve bookings"
                    )
                }
            }
        }
    }

    fun openDetailsModal(bookingDetails: BookingDetails) {
        _uiState.update {
            it.copy(
                isDetailModalOpen = true,
                selectedOrderDetails = bookingDetails
            )
        }
    }

    fun closeDetailsModal() {
        _uiState.update {
            it.copy(
                isDetailModalOpen = false,
                selectedOrderDetails = null
            )
        }
    }
}
