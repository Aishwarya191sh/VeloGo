package com.example.frontend.ui.screens.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.frontend.R
import com.example.frontend.domain.model.BookingDetails
import com.example.frontend.ui.components.VeloGoButton
import com.example.frontend.ui.components.shimmerLoading
import com.example.frontend.ui.theme.EmeraldPrimary
import com.example.frontend.ui.theme.SlateGrey
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun OrdersScreen(
    navController: NavController,
    viewModel: OrdersViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Active", "Completed", "Cancelled")

    // Dynamic filtering matching the backend status mapping
    val filteredBookings = remember(uiState.bookings, selectedTab) {
        when (selectedTab) {
            0 -> uiState.bookings.filter {
                it.booking.status in listOf("booked", "onTrip", "notPicked", "notBooked", "overDue")
            }
            1 -> uiState.bookings.filter {
                it.booking.status == "tripCompleted"
            }
            else -> uiState.bookings.filter {
                it.booking.status == "canceled"
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchBookings()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = EmeraldPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = EmeraldPrimary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        },
                        selectedContentColor = EmeraldPrimary,
                        unselectedContentColor = SlateGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                uiState.isLoading -> {
                    // Shimmer loader list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(4) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .shimmerLoading()
                            )
                        }
                    }
                }
                uiState.errorMessage != null -> {
                    // Retry panel
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.errorMessage ?: "Failed to load bookings",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            VeloGoButton(
                                text = "Retry",
                                onClick = { viewModel.fetchBookings() },
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    }
                }
                filteredBookings.isEmpty() -> {
                    // Empty state matching original React "No Bookings Yet"
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = SlateGrey.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Bookings Yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Check out our cars and book your first ride!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SlateGrey
                            )
                        }
                    }
                }
                else -> {
                    // Bookings list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(filteredBookings) { bookingDetails ->
                            OrderCard(
                                order = bookingDetails,
                                onDetailsClick = { viewModel.openDetailsModal(bookingDetails) }
                            )
                        }
                    }
                }
            }
        }

        // Details Modal/Dialog representing UserOrderDetailsModal
        if (uiState.isDetailModalOpen && uiState.selectedOrderDetails != null) {
            OrderDetailsDialog(
                orderDetails = uiState.selectedOrderDetails!!,
                onDismiss = { viewModel.closeDetailsModal() }
            )
        }
    }
}

@Composable
fun OrderCard(
    order: BookingDetails,
    onDetailsClick: () -> Unit
) {
    val statusLabel = when (order.booking.status) {
        "booked" -> "Booked"
        "onTrip" -> "On Trip"
        "notPicked" -> "Not Picked Up"
        "canceled" -> "Cancelled"
        "tripCompleted" -> "Completed"
        "overDue" -> "Overdue"
        else -> order.booking.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
    }

    val statusColor = when (order.booking.status) {
        "booked", "onTrip" -> EmeraldPrimary
        "tripCompleted" -> SlateGrey
        "canceled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.error
    }

    val pickupDateStr = formatIsoDate(order.booking.pickupDate)
    val dropoffDateStr = formatIsoDate(order.booking.dropOffDate)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SlateGrey.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Booking Ref and Status tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ref: ${order.booking.id.takeLast(8).uppercase(Locale.US)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = SlateGrey
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    contentColor = statusColor
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body: Image + Title + Dates + Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val imageUrl = order.vehicle.image.firstOrNull() ?: ""
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = order.vehicle.name ?: "Vehicle Image",
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .shimmerLoading()
                        )
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No Image", style = MaterialTheme.typography.bodySmall, color = SlateGrey)
                        }
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            if (isSystemInDarkTheme()) Color(0xFF242424) else Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.vehicle.name ?: "Premium Vehicle",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$pickupDateStr - $dropoffDateStr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateGrey
                    )
                }

                Text(
                    text = "₹${order.booking.totalPrice.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SlateGrey.copy(alpha = 0.1f))

            // Footer: Pick / Drop Locations Summary + Action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SlateGrey
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${order.booking.pickUpLocation} ➔ ${order.booking.dropOffLocation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateGrey,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onDetailsClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Details", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsDialog(
    orderDetails: BookingDetails,
    onDismiss: () -> Unit
) {
    val booking = orderDetails.booking
    val vehicle = orderDetails.vehicle

    val pickupDateStr = formatIsoDate(booking.pickupDate)
    val pickupTimeStr = formatIsoTime(booking.pickupDate)
    val dropoffDateStr = formatIsoDate(booking.dropOffDate)
    val dropoffTimeStr = formatIsoTime(booking.dropOffDate)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Dialog Header
                Text(
                    text = "Booking Summary",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SlateGrey.copy(alpha = 0.1f))

                // Scrollable content area for tablet compatibility
                Column(
                    modifier = Modifier
                        .weight(weight = 1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Section 1: Booking metrics
                    Text(
                        text = "Booking Details",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(label = "Booking ID", value = booking.id)
                    DetailRow(label = "Total Amount", value = "₹${booking.totalPrice.toInt()}")
                    DetailRow(label = "Pickup Location", value = booking.pickUpLocation)
                    DetailRow(label = "Pickup Date", value = "$pickupDateStr at $pickupTimeStr")
                    DetailRow(label = "Dropoff Location", value = booking.dropOffLocation)
                    DetailRow(label = "Dropoff Date", value = "$dropoffDateStr at $dropoffTimeStr")

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section 2: Vehicle Specs representing UserOrderDetailsModal
                    Text(
                        text = "Vehicle Details",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(label = "Vehicle Number", value = vehicle.registrationNumber)
                    DetailRow(label = "Model", value = vehicle.model ?: "Unknown")
                    DetailRow(label = "Company", value = vehicle.company ?: "Unknown")
                    DetailRow(label = "Vehicle Type", value = (vehicle.carType ?: "car").uppercase(Locale.US))
                    DetailRow(label = "Seats", value = "${vehicle.seats ?: 5} Seater")
                    DetailRow(label = "Fuel Type", value = (vehicle.fuelType ?: "petrol").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() })
                    DetailRow(label = "Transmission", value = (vehicle.transmission ?: "manual").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() })
                    DetailRow(label = "Manufacturing Year", value = (vehicle.yearMade ?: 2024).toString())
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action OK button
                VeloGoButton(
                    text = "Close",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = SlateGrey,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.5f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatIsoDate(isoString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = inputFormat.parse(isoString) ?: return isoString
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        outputFormat.format(date)
    } catch (e: Exception) {
        try {
            val inputFormat2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val date = inputFormat2.parse(isoString) ?: return isoString
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
            outputFormat.format(date)
        } catch (e2: Exception) {
            isoString
        }
    }
}

private fun formatIsoTime(isoString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = inputFormat.parse(isoString) ?: return isoString
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.US)
        outputFormat.format(date)
    } catch (e: Exception) {
        try {
            val inputFormat2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val date = inputFormat2.parse(isoString) ?: return isoString
            val outputFormat = SimpleDateFormat("hh:mm a", Locale.US)
            outputFormat.format(date)
        } catch (e2: Exception) {
            "10:00 AM"
        }
    }
}
