package com.example.frontend.ui.screens.search

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.frontend.ui.navigation.Screen
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.frontend.R
import com.example.frontend.domain.model.Vehicle
import com.example.frontend.ui.components.EmptyOrErrorStateView
import com.example.frontend.ui.components.RentARideButton
import com.example.frontend.ui.components.shimmerLoading
import com.example.frontend.ui.theme.EmeraldPrimary
import com.example.frontend.ui.theme.EmeraldPrimaryContainer
import com.example.frontend.ui.theme.OnEmeraldPrimaryContainer
import com.example.frontend.ui.theme.SlateGrey
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VehicleDetailsScreen(
    navController: NavController,
    vehicleId: String,
    searchFlowViewModel: SearchFlowViewModel,
    viewModel: VehicleDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchUiState by searchFlowViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // Trigger details loading
    LaunchedEffect(vehicleId) {
        viewModel.loadVehicleDetails(
            vehicleId = vehicleId,
            cachedVehicle = searchUiState.selectedVariant
        )
    }

    var showFullScreenViewer by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vehicle Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Native Android Share Sheet
                    IconButton(onClick = {
                        val vehicleName = uiState.vehicle?.name ?: "Premium Car"
                        val price = uiState.vehicle?.price?.toInt() ?: 0
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Check out this amazing $vehicleName for ₹$price/day on VeloGo!"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Vehicle via"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    VehicleDetailsShimmerLoader()
                }
                uiState.error != null -> {
                    EmptyOrErrorStateView(
                        title = "Error Loading Details",
                        message = uiState.error ?: "Something went wrong",
                        actionButtonText = "Retry",
                        onActionClick = {
                            viewModel.loadVehicleDetails(vehicleId, searchUiState.selectedVariant)
                        }
                    )
                }
                uiState.vehicle != null -> {
                    val vehicle = uiState.vehicle!!

                    // Calculate rental duration in days
                    val durationDays = remember(searchUiState.pickupDateTime, searchUiState.dropoffDateTime) {
                        val pickup = searchUiState.pickupDateTime
                        val dropoff = searchUiState.dropoffDateTime
                        if (pickup != null && dropoff != null) {
                            val diffMs = dropoff.timeInMillis - pickup.timeInMillis
                            (diffMs / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
                        } else {
                            1L
                        }
                    }

                    val totalPrice = remember(vehicle.price, durationDays) {
                        vehicle.price * durationDays
                    }

                    if (isTablet) {
                        // Responsive Layout for Tablet & Wide screens (Horizontal Split)
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Left side - Image Gallery
                            Column(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                ImageGalleryCarousel(
                                    images = vehicle.image,
                                    onImageClick = { index ->
                                        selectedImageIndex = index
                                        showFullScreenViewer = true
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                PickupInfoCard(searchUiState = searchUiState, durationDays = durationDays)
                            }

                            // Right side - Information Details & Bottom Bar Actions
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    VehicleTitleSection(vehicle = vehicle)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    SpecificationsGrid(vehicle = vehicle)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    VehicleDescriptionSection(vehicle = vehicle)
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                TabletBookingPanel(
                                    vehicle = vehicle,
                                    durationDays = durationDays,
                                    totalPrice = totalPrice,
                                    onBookClick = {
                                        navController.navigate(Screen.Checkout.route)
                                    }
                                )
                            }
                        }
                    } else {
                        // Standard Layout for Phones (Vertical Scroll with Sticky Bottom Bar)
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(bottom = 88.dp) // Leave space for sticky bottom bar
                            ) {
                                ImageGalleryCarousel(
                                    images = vehicle.image,
                                    onImageClick = { index ->
                                        selectedImageIndex = index
                                        showFullScreenViewer = true
                                    }
                                )

                                Column(modifier = Modifier.padding(16.dp)) {
                                    VehicleTitleSection(vehicle = vehicle)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    SpecificationsGrid(vehicle = vehicle)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    VehicleDescriptionSection(vehicle = vehicle)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    PickupInfoCard(searchUiState = searchUiState, durationDays = durationDays)
                                }
                            }

                            // Sticky Bottom Booking Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .navigationBarsPadding()
                                    .border(1.dp, SlateGrey.copy(alpha = 0.12f))
                                    .padding(16.dp)
                            ) {
                                StickyBottomBookingBar(
                                    vehicle = vehicle,
                                    durationDays = durationDays,
                                    totalPrice = totalPrice,
                                    onBookClick = {
                                        navController.navigate(Screen.Checkout.route)
                                    }
                                )
                            }
                        }
                    }

                    // Full-Screen Image Viewer Dialog
                    if (showFullScreenViewer) {
                        FullScreenImageViewerDialog(
                            images = vehicle.image,
                            initialIndex = selectedImageIndex,
                            onDismiss = { showFullScreenViewer = false }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Component Layouts
// -------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageGalleryCarousel(
    images: List<String>,
    onImageClick: (Int) -> Unit
) {
    if (images.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(64.dp), tint = SlateGrey)
        }
    } else {
        val pagerState = rememberPagerState(pageCount = { images.size })
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(if (isSystemInDarkTheme()) Color(0xFF151816) else Color(0xFFF1F5F2))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onImageClick(page) },
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(images[page])
                            .crossfade(true)
                            .build(),
                        contentDescription = "Vehicle image $page",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
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
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error loading image",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }
            }

            // Carousel dots indicator
            if (images.size > 1) {
                Row(
                    Modifier
                        .height(32.dp)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(images.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) EmeraldPrimary else SlateGrey.copy(alpha = 0.5f)
                        val width = if (pagerState.currentPage == iteration) 18.dp else 6.dp
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .clip(CircleShape)
                                .background(color)
                                .height(6.dp)
                                .width(width)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleTitleSection(vehicle: Vehicle) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // Brand name
            Text(
                text = vehicle.company?.uppercase(Locale.getDefault()) ?: "VELOGO",
                style = MaterialTheme.typography.labelSmall,
                color = EmeraldPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            // Model + Variant
            Text(
                text = "${vehicle.name ?: "Premium Vehicle"} ${vehicle.model ?: ""}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Registration number
            Text(
                text = "Reg No: ${vehicle.registrationNumber}",
                style = MaterialTheme.typography.bodySmall,
                color = SlateGrey
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            // Price / Day
            Text(
                text = "₹${vehicle.price.toInt()}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
            )
            Text(
                text = "per day",
                style = MaterialTheme.typography.labelSmall,
                color = SlateGrey
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Badges Row (Rating and Availability)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rating Badge
        val ratingVal = vehicle.rating ?: "5"
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(EmeraldPrimaryContainer)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Rating",
                tint = OnEmeraldPrimaryContainer,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$ratingVal.0 Rating",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = OnEmeraldPrimaryContainer
            )
        }

        // Availability Badge
        val isBooked = vehicle.isBooked
        val statusText = if (isBooked) "Reserved / Booked" else "Available"
        val statusBg = if (isBooked) MaterialTheme.colorScheme.errorContainer else EmeraldPrimaryContainer
        val statusColor = if (isBooked) MaterialTheme.colorScheme.onErrorContainer else OnEmeraldPrimaryContainer

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(statusBg)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}

@Composable
fun SpecificationsGrid(vehicle: Vehicle) {
    Text(
        text = "Specifications",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpecificationCard(
                icon = Icons.Default.Person,
                label = "Seats Capacity",
                value = "${vehicle.seats ?: 5} Seats",
                modifier = Modifier.weight(1f)
            )
            SpecificationCard(
                icon = Icons.Default.Settings,
                label = "Transmission",
                value = vehicle.transmission?.replaceFirstChar { it.uppercase() } ?: "Manual",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpecificationCard(
                icon = Icons.Default.Info,
                label = "Fuel Type",
                value = vehicle.fuelType?.replaceFirstChar { it.uppercase() } ?: "Petrol",
                modifier = Modifier.weight(1f)
            )
            SpecificationCard(
                icon = Icons.Default.List,
                label = "Vehicle Type",
                value = vehicle.carType?.uppercase(Locale.getDefault()) ?: "SEDAN",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SpecificationCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SlateGrey.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateGrey
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun VehicleDescriptionSection(vehicle: Vehicle) {
    var isExpanded by remember { mutableStateOf(false) }
    val description = vehicle.carDescription ?: vehicle.description ?: "Enjoy a premium and smooth driving experience with this selected model. Fully serviced and ready to roll."

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SlateGrey.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Vehicle Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Justify
            )

            // Read More Button
            if (description.length > 120) {
                Text(
                    text = if (isExpanded) "Read Less" else "Read More",
                    color = EmeraldPrimary,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .clickable { isExpanded = !isExpanded }
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PickupInfoCard(
    searchUiState: SearchUiState,
    durationDays: Long
) {
    val formatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemInDarkTheme()) Color(0xFF141A16) else Color(0xFFEAF9EE)
        ),
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rental Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSystemInDarkTheme()) Color.White else OnEmeraldPrimaryContainer
                )

                // Duration tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(EmeraldPrimary)
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

            // Pick up
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "PICK-UP LOCATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateGrey,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${searchUiState.pickupLocation}, ${searchUiState.selectedDistrict}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    searchUiState.pickupDateTime?.let {
                        Text(
                            text = formatter.format(it.time),
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateGrey
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = EmeraldPrimary.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            // Drop off
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
                        text = "DROP-OFF LOCATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateGrey,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${searchUiState.dropoffLocation}, ${searchUiState.selectedDistrict}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    searchUiState.dropoffDateTime?.let {
                        Text(
                            text = formatter.format(it.time),
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateGrey
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StickyBottomBookingBar(
    vehicle: Vehicle,
    durationDays: Long,
    totalPrice: Double,
    onBookClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "ESTIMATED TOTAL",
                style = MaterialTheme.typography.labelSmall,
                color = SlateGrey,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "₹${totalPrice.toInt()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
                Text(
                    text = " / $durationDays ${if (durationDays == 1L) "day" else "days"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateGrey,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
        }

        RentARideButton(
            text = "Book Now",
            onClick = onBookClick,
            modifier = Modifier.width(160.dp),
            enabled = !vehicle.isBooked
        )
    }
}

@Composable
fun TabletBookingPanel(
    vehicle: Vehicle,
    durationDays: Long,
    totalPrice: Double,
    onBookClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SlateGrey.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Pricing Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Daily Rate", style = MaterialTheme.typography.bodyMedium, color = SlateGrey)
                Text(text = "₹${vehicle.price.toInt()} / Day", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Rental Duration", style = MaterialTheme.typography.bodyMedium, color = SlateGrey)
                Text(text = "$durationDays Days", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }

            Divider(color = SlateGrey.copy(alpha = 0.12f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Total Price", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "₹${totalPrice.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            RentARideButton(
                text = "Book Now",
                onClick = onBookClick,
                enabled = !vehicle.isBooked,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// -------------------------------------------------------------
// Shimmer Skeleton Loader Layout
// -------------------------------------------------------------

@Composable
fun VehicleDetailsShimmerLoader() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Carousel Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmerLoading()
        )

        // Title Shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerLoading()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerLoading()
                )
            }
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerLoading()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Specs Shimmer
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f).height(60.dp).clip(RoundedCornerShape(12.dp)).shimmerLoading())
                Box(modifier = Modifier.weight(1f).height(60.dp).clip(RoundedCornerShape(12.dp)).shimmerLoading())
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f).height(60.dp).clip(RoundedCornerShape(12.dp)).shimmerLoading())
                Box(modifier = Modifier.weight(1f).height(60.dp).clip(RoundedCornerShape(12.dp)).shimmerLoading())
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Description Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmerLoading()
        )
    }
}

// -------------------------------------------------------------
// Zoomable Image Full Screen Dialog
// -------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenImageViewerDialog(
    images: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { images.size })

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                ZoomableImage(
                    imageUrl = images[page],
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // Close button overlay
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close full-screen image",
                    tint = Color.White
                )
            }

            // Image Counter Overlay
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${images.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ZoomableImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        val extraWidth = (scale - 1) * 350
        val extraHeight = (scale - 1) * 350
        offset = Offset(
            x = (offset.x + offsetChange.x).coerceIn(-extraWidth, extraWidth),
            y = (offset.y + offsetChange.y).coerceIn(-extraHeight, extraHeight)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = if (scale > 1f) 1f else 2.5f
                        offset = Offset.Zero
                    }
                )
            }
            .transformable(state = state)
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = contentScale,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
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
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error loading image",
                        tint = Color.Red
                    )
                }
            }
        )
    }
}
