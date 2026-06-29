package com.example.frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.frontend.data.local.pref.AppTheme
import com.example.frontend.data.local.pref.SettingsManager
import com.example.frontend.ui.navigation.AppNavigation
import com.example.frontend.ui.screens.auth.AuthViewModel
import com.example.frontend.ui.screens.booking.CheckoutViewModel
import com.example.frontend.ui.screens.search.SearchFlowViewModel
import com.example.frontend.ui.theme.FrontEndTheme
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

    @Inject
    lateinit var settingsManager: SettingsManager

    private val checkoutViewModel: CheckoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialTheme = runBlocking { settingsManager.themeFlow.first() }
        setContent {
            val appTheme by settingsManager.themeFlow.collectAsState(initial = initialTheme)
            val isDarkTheme = when (appTheme) {
                AppTheme.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }

            FrontEndTheme(darkTheme = isDarkTheme) {
                val authViewModel: AuthViewModel = hiltViewModel()
                val searchViewModel: SearchFlowViewModel = hiltViewModel()

                AppNavigation(
                    authViewModel = authViewModel,
                    searchViewModel = searchViewModel
                )
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        val paymentId = razorpayPaymentId ?: ""
        val orderId = paymentData?.orderId ?: ""
        val signature = paymentData?.signature ?: ""
        checkoutViewModel.onPaymentSuccess(paymentId, orderId, signature)
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        val message = response ?: "Payment cancelled or failed"
        checkoutViewModel.onPaymentError(message)
    }
}
