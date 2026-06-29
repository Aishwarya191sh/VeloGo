package com.example.frontend.ui.screens.booking

import androidx.activity.ComponentActivity
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.frontend.R
import com.example.frontend.ui.components.EmptyOrErrorStateView
import com.example.frontend.ui.components.RentARideButton
import com.example.frontend.ui.components.RentARideTextField
import com.example.frontend.ui.screens.search.SearchFlowViewModel
import com.example.frontend.ui.util.Constants
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    searchFlowViewModel: SearchFlowViewModel,
    viewModel: CheckoutViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchState by searchFlowViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val vehicle = searchState.selectedVariant
    val pickup = searchState.pickupDateTime
    val dropoff = searchState.dropoffDateTime

    // Calculate rental duration in days
    val durationDays = remember(pickup, dropoff) {
        if (pickup != null && dropoff != null) {
            val diffMs = dropoff.timeInMillis - pickup.timeInMillis
            (diffMs / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
        } else {
            1L
        }
    }

    // Set up ViewModel parameters when variant or dates are cached
    LaunchedEffect(vehicle, durationDays) {
        if (vehicle != null) {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            viewModel.setupBookingParameters(
                pricePerDay = vehicle.price,
                rentalDays = durationDays,
                vehicleId = vehicle.id,
                pickupDate = pickup?.let { formatter.format(it.time) } ?: "",
                dropoffDate = dropoff?.let { formatter.format(it.time) } ?: "",
                pickupLocation = searchState.pickupLocation,
                dropoffLocation = searchState.dropoffLocation,
                pickupDistrict = searchState.selectedDistrict
            )
        }
    }

    // Observe payment success state to route to success screen
    LaunchedEffect(uiState.paymentSuccess) {
        if (uiState.paymentSuccess) {
            navController.navigate("booking_success") {
                popUpTo("checkout") { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout Summary", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        if (vehicle == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                EmptyOrErrorStateView(
                    title = "Checkout Unavailable",
                    message = "No selected vehicle was found. Please select a vehicle from catalog.",
                    actionButtonText = "Back to Home",
                    onActionClick = {
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Outer scroll container
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 100.dp) // Space for sticky booking bar
                ) {
                    if (isTablet) {
                        // Wide screen / Tablet split grid layout
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Column(modifier = Modifier.weight(1.1f)) {
                                OrderSummaryCard(vehicle = vehicle)
                                Spacer(modifier = Modifier.height(16.dp))
                                RentalDatesCard(searchState = searchState, durationDays = durationDays)
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                PaymentFormCard(viewModel = viewModel, uiState = uiState)
                                PriceCalculationCard(viewModel = viewModel, uiState = uiState)
                            }
                        }
                    } else {
                        // Phone single column vertical scroll layout
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OrderSummaryCard(vehicle = vehicle)
                            RentalDatesCard(searchState = searchState, durationDays = durationDays)
                            PaymentFormCard(viewModel = viewModel, uiState = uiState)
                            PriceCalculationCard(viewModel = viewModel, uiState = uiState)
                        }
                    }
                }

                // Sticky Bottom Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(MaterialTheme.colorScheme.surface)
                        .navigationBarsPadding()
                        .border(1.dp, Color.LightGray.copy(alpha = 0.2f))
                        .padding(16.dp)
                ) {
                    val finalTotal = viewModel.calculateTotal()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                            .align(Alignment.Center),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL PAYABLE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₹${finalTotal.toInt()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (uiState.isRecoveryState) {
                            // Recovery Retry button
                            RentARideButton(
                                text = "Retry Booking",
                                onClick = { viewModel.retrySaveBooking() },
                                modifier = Modifier.width(180.dp),
                                isLoading = uiState.isProcessing
                            )
                        } else {
                            // Standard Checkout order button
                            RentARideButton(
                                text = "Place Order",
                                onClick = {
                                    viewModel.startPlaceOrder { orderId, totalAmount ->
                                        // Launch native Razorpay SDK checkout flow
                                        val activity = context as ComponentActivity
                                        val co = com.razorpay.Checkout()
                                        co.setKeyID(Constants.RAZORPAY_KEY_ID)
                                        val options = JSONObject().apply {
                                            put("name", "VeloGo")
                                            put("description", "Vehicle Rental Booking Payment")
                                            put("order_id", orderId)
                                            put("currency", Constants.DEFAULT_CURRENCY)
                                            put("amount", (totalAmount * 100).toLong().toString())
                                            put("prefill", JSONObject().apply {
                                                put("email", uiState.email)
                                                put("contact", uiState.phoneNumber)
                                            })
                                            put("theme", JSONObject().apply {
                                                put("color", "#00C853") // Emerald Green Primary Color
                                            })
                                        }
                                        co.open(activity, options)
                                    }
                                },
                                modifier = Modifier.width(180.dp),
                                isLoading = uiState.isProcessing
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderSummaryCard(vehicle: com.example.frontend.domain.model.Vehicle) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSystemInDarkTheme()) Color(0xFF22351A) else Color(0xFFF1F8E9)),
                    contentAlignment = Alignment.Center
                ) {
                    val imageUrl = vehicle.image.firstOrNull()
                    if (imageUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = vehicle.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.empty_vehicle),
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${vehicle.company?.uppercase(Locale.getDefault()) ?: "VELOGO"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${vehicle.name ?: "Premium Car"} ${vehicle.model ?: ""}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Registration: ${vehicle.registrationNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${vehicle.price.toInt()} / Day",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun RentalDatesCard(
    searchState: com.example.frontend.ui.screens.search.SearchUiState,
    durationDays: Long
) {
    val formatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rental Duration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$durationDays ${if (durationDays == 1L) "Day" else "Days"}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "PICK-UP",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${searchState.pickupLocation}, ${searchState.selectedDistrict}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    searchState.pickupDateTime?.let {
                        Text(
                            text = formatter.format(it.time),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "DROP-OFF",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${searchState.dropoffLocation}, ${searchState.selectedDistrict}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    searchState.dropoffDateTime?.let {
                        Text(
                            text = formatter.format(it.time),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentFormCard(
    viewModel: CheckoutViewModel,
    uiState: CheckoutUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Contact & Billing Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // General Warning/Error panel
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error icon",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close error",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            RentARideTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = "Billing Email",
                errorText = uiState.emailError,
                leadingIcon = Icons.Default.Email
            )

            RentARideTextField(
                value = uiState.phoneNumber,
                onValueChange = { viewModel.onPhoneChange(it) },
                label = "Billing Contact Phone",
                errorText = uiState.phoneNumberError,
                leadingIcon = Icons.Default.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            RentARideTextField(
                value = uiState.address,
                onValueChange = { viewModel.onAddressChange(it) },
                label = "Billing Address",
                errorText = uiState.addressError,
                leadingIcon = Icons.Default.Home,
                singleLine = false
            )

            // Coupon Code Application Row
            Column {
                Text(
                    text = "Apply Coupon Code",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.coupon,
                        onValueChange = { viewModel.onCouponChange(it) },
                        placeholder = { Text("e.g. WELCOME50") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    Button(
                        onClick = { viewModel.applyCoupon() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground)
                    ) {
                        Text("Apply", color = MaterialTheme.colorScheme.background)
                    }
                }

                if (uiState.wrongCoupon) {
                    Text(
                        text = "Invalid coupon code. Try WELCOME50",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (uiState.discount > 0) {
                    Text(
                        text = "Coupon WELCOME50 applied! ₹${uiState.discount.toInt()} discount credited.",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PriceCalculationCard(
    viewModel: CheckoutViewModel,
    uiState: CheckoutUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Fare Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            val rentTotal = uiState.pricePerDay * uiState.rentalDays
            val finalTotal = viewModel.calculateTotal()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Daily Rate (₹${uiState.pricePerDay.toInt()} x ${uiState.rentalDays} Days)", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(text = "₹${rentTotal.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Platform Fee", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(text = "₹${Constants.PLATFORM_FEE.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }

            if (uiState.discount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Discount", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Text(text = "-₹${uiState.discount.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Final Payable", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "₹${finalTotal.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
