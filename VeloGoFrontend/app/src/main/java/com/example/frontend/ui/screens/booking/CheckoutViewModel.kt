package com.example.frontend.ui.screens.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.remote.dto.RazorpayOrderResponse
import com.example.frontend.domain.model.BookingDetails
import com.example.frontend.domain.repository.AuthRepository
import com.example.frontend.domain.repository.BookingRepository
import com.example.frontend.ui.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutUiState(
    val email: String = "",
    val emailError: String? = null,
    val phoneNumber: String = "",
    val phoneNumberError: String? = null,
    val address: String = "",
    val addressError: String? = null,
    
    val coupon: String = "",
    val discount: Double = 0.0,
    val wrongCoupon: Boolean = false,
    
    val isProcessing: Boolean = false,
    val error: String? = null,
    val paymentSuccess: Boolean = false,
    
    val razorpayOrderId: String? = null,
    val razorpayAmount: Int = 0,
    
    // Booking details successfully fetched after completion
    val bookingDetails: BookingDetails? = null,
    
    // Recovery state when Razorpay succeeds but bookCar fails
    val isRecoveryState: Boolean = false,
    val savedPaymentId: String = "",
    val savedOrderId: String = "",
    
    // For calculating dynamic price breakdown
    val rentalDays: Long = 1,
    val pricePerDay: Double = 0.0,
    val userId: String = "",
    val vehicleId: String = "",
    val pickupDate: String = "",
    val dropoffDate: String = "",
    val pickupLocation: String = "",
    val dropoffLocation: String = "",
    val pickupDistrict: String = ""
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        // Collect current session to prefill details
        viewModelScope.launch {
            authRepository.getSessionDetails().collect { user ->
                user?.let {
                    _uiState.update { state ->
                        state.copy(
                            userId = user.id,
                            email = user.email,
                            phoneNumber = "" // Can prefill if user object holds phoneNumber, otherwise empty
                        )
                    }
                }
            }
        }
    }

    fun setupBookingParameters(
        pricePerDay: Double,
        rentalDays: Long,
        vehicleId: String,
        pickupDate: String,
        dropoffDate: String,
        pickupLocation: String,
        dropoffLocation: String,
        pickupDistrict: String
    ) {
        _uiState.update {
            it.copy(
                pricePerDay = pricePerDay,
                rentalDays = rentalDays,
                vehicleId = vehicleId,
                pickupDate = pickupDate,
                dropoffDate = dropoffDate,
                pickupLocation = pickupLocation,
                dropoffLocation = dropoffLocation,
                pickupDistrict = pickupDistrict
            )
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone, phoneNumberError = null) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address, addressError = null) }
    }

    fun onCouponChange(coupon: String) {
        _uiState.update { it.copy(coupon = coupon, wrongCoupon = false) }
    }

    fun applyCoupon() {
        val currentCoupon = _uiState.value.coupon.trim()
        if (currentCoupon.equals(Constants.VALID_COUPON_CODE, ignoreCase = true)) {
            _uiState.update { it.copy(discount = Constants.COUPON_DISCOUNT, wrongCoupon = false) }
        } else {
            _uiState.update { it.copy(discount = 0.0, wrongCoupon = true) }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val email = _uiState.value.email.trim()
        val phone = _uiState.value.phoneNumber.trim()
        val address = _uiState.value.address.trim()

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(emailError = "Please enter a valid email address") }
            isValid = false
        }
        if (phone.length < 8) {
            _uiState.update { it.copy(phoneNumberError = "Please enter a valid phone number (min 8 digits)") }
            isValid = false
        }
        if (address.length < 4) {
            _uiState.update { it.copy(addressError = "Please enter your address (min 4 characters)") }
            isValid = false
        }

        return isValid
    }

    fun calculateTotal(): Double {
        val state = _uiState.value
        val rentTotal = state.pricePerDay * state.rentalDays
        val finalAmount = rentTotal + Constants.PLATFORM_FEE - state.discount
        return finalAmount.coerceAtLeast(0.0)
    }

    // Phase 1: Call API to create a Razorpay Order ID
    fun startPlaceOrder(onOrderCreated: (String, Double) -> Unit) {
        if (!validateInputs()) return
        if (_uiState.value.isProcessing) return // Prevent duplicate taps

        _uiState.update { it.copy(isProcessing = true, error = null) }
        val total = calculateTotal()

        viewModelScope.launch {
            bookingRepository.createRazorpayOrder(
                totalPrice = total,
                dropoffLocation = _uiState.value.dropoffLocation,
                pickupDistrict = _uiState.value.pickupDistrict,
                pickupLocation = _uiState.value.pickupLocation
            ).onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        razorpayOrderId = response.id,
                        razorpayAmount = response.amount
                    )
                }
                onOrderCreated(response.id, total)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = error.localizedMessage ?: "Failed to generate payment request"
                    )
                }
            }
        }
    }

    // Phase 2: Called from MainActivity on Razorpay payment success
    fun onPaymentSuccess(paymentId: String, orderId: String, signature: String) {
        if (_uiState.value.isProcessing) return // Prevent duplicate processing

        _uiState.update {
            it.copy(
                isProcessing = true,
                error = null,
                savedPaymentId = paymentId,
                savedOrderId = orderId
            )
        }

        executeSaveBooking(paymentId, orderId)
    }

    fun retrySaveBooking() {
        val state = _uiState.value
        if (state.savedPaymentId.isEmpty() || state.savedOrderId.isEmpty()) {
            _uiState.update { it.copy(error = "No active transaction to recover.") }
            return
        }

        _uiState.update { it.copy(isProcessing = true, error = null) }
        executeSaveBooking(state.savedPaymentId, state.savedOrderId)
    }

    private fun executeSaveBooking(paymentId: String, orderId: String) {
        val state = _uiState.value
        val total = calculateTotal()

        viewModelScope.launch {
            bookingRepository.bookCar(
                userId = state.userId,
                vehicleId = state.vehicleId,
                totalPrice = total,
                pickupDate = state.pickupDate,
                dropoffDate = state.dropoffDate,
                pickupLocation = state.pickupLocation,
                dropoffLocation = state.dropoffLocation,
                pickupDistrict = state.pickupDistrict,
                razorpayPaymentId = paymentId,
                razorpayOrderId = orderId
            ).onSuccess { booking ->
                // Fetch the latest booking populated with vehicle object
                fetchLatestBooking(state.userId)
            }.onFailure { error ->
                // Payment succeeded, but booking save failed -> Transition to Recovery State
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        isRecoveryState = true,
                        error = "Payment Successful (ID: $paymentId), but we failed to record your booking. Please click retry to complete booking: ${error.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun fetchLatestBooking(userId: String) {
        viewModelScope.launch {
            bookingRepository.getLatestBooking(userId)
                .onSuccess { details ->
                    if (details != null) {
                        _uiState.update { it.copy(bookingDetails = details) }
                        // Send confirmation email
                        sendConfirmationEmail(details)
                    } else {
                        // Edge case: successfully booked but latest booking is missing
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                paymentSuccess = true,
                                error = null
                            )
                        }
                    }
                }
                .onFailure { error ->
                    // Fallback to success page since payment and booking save are complete
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            paymentSuccess = true,
                            error = null
                        )
                    }
                }
        }
    }

    private fun sendConfirmationEmail(details: BookingDetails) {
        val recipientEmail = _uiState.value.email.trim()
        viewModelScope.launch {
            bookingRepository.sendBookingEmail(recipientEmail, details)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            paymentSuccess = true,
                            error = null
                        )
                    }
                }
                .onFailure {
                    // Fail gracefully and transition to success screen since booking is recorded
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            paymentSuccess = true,
                            error = null
                        )
                    }
                }
        }
    }

    // Called from MainActivity on Razorpay payment failure/cancellation
    fun onPaymentError(message: String) {
        _uiState.update {
            it.copy(
                isProcessing = false,
                error = message
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
