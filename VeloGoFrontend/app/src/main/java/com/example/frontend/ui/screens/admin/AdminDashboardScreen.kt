package com.example.frontend.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.frontend.domain.model.Vehicle
import com.example.frontend.domain.model.VendorBooking
import com.example.frontend.ui.theme.EmeraldPrimary
import com.example.frontend.ui.theme.SlateGrey
import com.example.frontend.ui.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel(),
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val userSession by viewModel.currentSession.collectAsState()
    val requestsState by viewModel.vendorRequestsState.collectAsState()
    val listingsState by viewModel.approvedVehiclesState.collectAsState()
    val bookingsState by viewModel.bookingsState.collectAsState()

    val approveAction by viewModel.approveState.collectAsState()
    val rejectAction by viewModel.rejectState.collectAsState()
    val deleteAction by viewModel.deleteState.collectAsState()
    val changeStatusAction by viewModel.changeStatusState.collectAsState()

    var activeTab by remember { mutableStateOf(0) }
    var selectedBookingForDetails by remember { mutableStateOf<VendorBooking?>(null) }

    // Toast updates
    LaunchedEffect(approveAction) {
        if (approveAction is Resource.Success && (approveAction as Resource.Success<String>).data.isNotEmpty()) {
            Toast.makeText(context, (approveAction as Resource.Success<String>).data, Toast.LENGTH_SHORT).show()
            viewModel.resetActionStates()
        } else if (approveAction is Resource.Error) {
            Toast.makeText(context, (approveAction as Resource.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetActionStates()
        }
    }

    LaunchedEffect(rejectAction) {
        if (rejectAction is Resource.Success && (rejectAction as Resource.Success<String>).data.isNotEmpty()) {
            Toast.makeText(context, (rejectAction as Resource.Success<String>).data, Toast.LENGTH_SHORT).show()
            viewModel.resetActionStates()
        } else if (rejectAction is Resource.Error) {
            Toast.makeText(context, (rejectAction as Resource.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetActionStates()
        }
    }

    LaunchedEffect(deleteAction) {
        if (deleteAction is Resource.Success && (deleteAction as Resource.Success<String>).data.isNotEmpty()) {
            Toast.makeText(context, (deleteAction as Resource.Success<String>).data, Toast.LENGTH_SHORT).show()
            viewModel.resetActionStates()
        } else if (deleteAction is Resource.Error) {
            Toast.makeText(context, (deleteAction as Resource.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetActionStates()
        }
    }

    LaunchedEffect(changeStatusAction) {
        if (changeStatusAction is Resource.Success && (changeStatusAction as Resource.Success<String>).data.isNotEmpty()) {
            Toast.makeText(context, (changeStatusAction as Resource.Success<String>).data, Toast.LENGTH_SHORT).show()
            viewModel.resetActionStates()
        } else if (changeStatusAction is Resource.Error) {
            Toast.makeText(context, (changeStatusAction as Resource.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetActionStates()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Admin Dashboard", fontWeight = FontWeight.Bold)
                        Text(
                            text = userSession?.email ?: "Administrator",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateGrey
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Admin Tabs
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = EmeraldPrimary
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Requests", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("All Listings", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("All Bookings", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (activeTab) {
                0 -> RequestsTabContent(
                    state = requestsState,
                    onApprove = { viewModel.approveVehicleRequest(it.id) },
                    onReject = { viewModel.rejectVehicleRequest(it.id) },
                    onRetry = { viewModel.fetchVendorRequests() }
                )
                1 -> ListingsTabContent(
                    state = listingsState,
                    onDelete = { viewModel.deleteVehicle(it.id) },
                    onRetry = { viewModel.fetchApprovedVehicles() }
                )
                2 -> BookingsTabContent(
                    state = bookingsState,
                    onDetails = { selectedBookingForDetails = it },
                    onChangeStatus = { id, status -> viewModel.changeBookingStatus(id, status) },
                    onRetry = { viewModel.fetchBookings() }
                )
            }
        }
    }

    selectedBookingForDetails?.let { booking ->
        AdminBookingDetailsDialog(
            booking = booking,
            onDismiss = { selectedBookingForDetails = null }
        )
    }
}

@Composable
fun RequestsTabContent(
    state: Resource<List<Vehicle>>,
    onApprove: (Vehicle) -> Unit,
    onReject: (Vehicle) -> Unit,
    onRetry: () -> Unit
) {
    when (state) {
        is Resource.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
        }
        is Resource.Error -> {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)) {
                        Text("Retry", color = Color.Black)
                    }
                }
            }
        }
        is Resource.Success -> {
            val requests = state.data.filter { !it.isAdminApproved && !it.isRejected }
            if (requests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = SlateGrey.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No pending requests",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "New vendor submissions will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateGrey
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(requests) { vehicle ->
                        RequestItemCard(
                            vehicle = vehicle,
                            onApprove = { onApprove(vehicle) },
                            onReject = { onReject(vehicle) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RequestItemCard(
    vehicle: Vehicle,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    var showApproveConfirm by remember { mutableStateOf(false) }
    var showRejectConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SlateGrey.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val imageUrl = vehicle.image.firstOrNull()
                AsyncImage(
                    model = imageUrl,
                    contentDescription = vehicle.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${vehicle.company} ${vehicle.name}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Reg: ${vehicle.registrationNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateGrey
                    )
                    Text(
                        text = "${vehicle.fuelType?.uppercase()} • ${vehicle.transmission?.uppercase()} • ${vehicle.seats} seats",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateGrey
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFFDE7), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "PENDING",
                        color = Color(0xFFF57F17),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Divider(color = SlateGrey.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${vehicle.price}/day",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showRejectConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Reject", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showApproveConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Approve", modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }

    if (showApproveConfirm) {
        AlertDialog(
            onDismissRequest = { showApproveConfirm = false },
            title = { Text("Approve Vehicle Request") },
            text = { Text("Are you sure you want to approve this vehicle listing? It will become publicly searchable and bookable.") },
            confirmButton = {
                Button(
                    onClick = {
                        showApproveConfirm = false
                        onApprove()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Approve", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveConfirm = false }) {
                    Text("Cancel", color = SlateGrey)
                }
            }
        )
    }

    if (showRejectConfirm) {
        AlertDialog(
            onDismissRequest = { showRejectConfirm = false },
            title = { Text("Reject Vehicle Request") },
            text = { Text("Are you sure you want to reject this vehicle listing? The vendor will see it as rejected.") },
            confirmButton = {
                Button(
                    onClick = {
                        showRejectConfirm = false
                        onReject()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectConfirm = false }) {
                    Text("Cancel", color = SlateGrey)
                }
            }
        )
    }
}

@Composable
fun ListingsTabContent(
    state: Resource<List<Vehicle>>,
    onDelete: (Vehicle) -> Unit,
    onRetry: () -> Unit
) {
    when (state) {
        is Resource.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
        }
        is Resource.Error -> {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)) {
                        Text("Retry", color = Color.Black)
                    }
                }
            }
        }
        is Resource.Success -> {
            val vehicles = state.data.filter { it.isDeleted == "false" && it.isAdminApproved }
            if (vehicles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = SlateGrey.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No listings available",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(vehicles) { vehicle ->
                        ListingItemCard(
                            vehicle = vehicle,
                            onDelete = { onDelete(vehicle) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ListingItemCard(
    vehicle: Vehicle,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SlateGrey.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val imageUrl = vehicle.image.firstOrNull()
                AsyncImage(
                    model = imageUrl,
                    contentDescription = vehicle.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${vehicle.company} ${vehicle.name}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Reg: ${vehicle.registrationNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateGrey
                    )
                    Text(
                        text = "${vehicle.fuelType?.uppercase()} • ${vehicle.transmission?.uppercase()} • ${vehicle.seats} seats",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateGrey
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "APPROVED",
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Divider(color = SlateGrey.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${vehicle.price}/day",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                )

                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Vehicle Listing") },
            text = { Text("Are you sure you want to remove this vehicle listing? This action is permanent.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = SlateGrey)
                }
            }
        )
    }
}

@Composable
fun BookingsTabContent(
    state: Resource<List<VendorBooking>>,
    onDetails: (VendorBooking) -> Unit,
    onChangeStatus: (String, String) -> Unit,
    onRetry: () -> Unit
) {
    when (state) {
        is Resource.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
        }
        is Resource.Error -> {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)) {
                        Text("Retry", color = Color.Black)
                    }
                }
            }
        }
        is Resource.Success -> {
            val bookings = state.data
            if (bookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = SlateGrey.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No bookings found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(bookings) { booking ->
                        AdminBookingItemCard(
                            booking = booking,
                            onDetails = { onDetails(booking) },
                            onChangeStatus = { status -> onChangeStatus(booking.id, status) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminBookingItemCard(
    booking: VendorBooking,
    onDetails: () -> Unit,
    onChangeStatus: (String) -> Unit
) {
    var statusExpanded by remember { mutableStateOf(false) }
    val statuses = listOf("notBooked", "booked", "onTrip", "notPicked", "canceled", "overDue", "tripCompleted")

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onDetails() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SlateGrey.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val imageUrl = booking.vehicleDetails.image.firstOrNull()
                AsyncImage(
                    model = imageUrl,
                    contentDescription = booking.vehicleDetails.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Booking #${booking.id.takeLast(6)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${booking.vehicleDetails.company} ${booking.vehicleDetails.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateGrey
                    )
                    Text(
                        text = "Total Price: ₹${booking.totalPrice}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .background(EmeraldPrimary.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = booking.status.uppercase(),
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Divider(color = SlateGrey.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Change Status", style = MaterialTheme.typography.labelSmall, color = SlateGrey)
                    Box(modifier = Modifier.padding(top = 4.dp)) {
                        Surface(
                            modifier = Modifier.clickable { statusExpanded = true },
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(booking.status, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("▼", fontSize = 10.sp, color = EmeraldPrimary)
                            }
                        }

                        DropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            statuses.forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st) },
                                    onClick = {
                                        statusExpanded = false
                                        if (st != booking.status) {
                                            onChangeStatus(st)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                TextButton(onClick = onDetails) {
                    Text("View Details", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminBookingDetailsDialog(
    booking: VendorBooking,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Booking Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminDetailRow(label = "Booking ID", value = booking.id)
                AdminDetailRow(label = "Vehicle", value = "${booking.vehicleDetails.company} ${booking.vehicleDetails.name}")
                AdminDetailRow(label = "Registration Number", value = booking.vehicleDetails.registrationNumber)
                AdminDetailRow(label = "Pickup Date", value = booking.pickupDate)
                AdminDetailRow(label = "Drop-off Date", value = booking.dropOffDate)
                AdminDetailRow(label = "Pickup Location", value = booking.pickUpLocation)
                AdminDetailRow(label = "Drop-off Location", value = booking.dropOffLocation)
                AdminDetailRow(label = "Total Price", value = "₹${booking.totalPrice}")
                AdminDetailRow(label = "Payment ID", value = booking.razorpayPaymentId ?: "N/A")
                AdminDetailRow(label = "Order ID", value = booking.razorpayOrderId ?: "N/A")
                AdminDetailRow(label = "Booking Status", value = booking.status.uppercase())
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = EmeraldPrimary)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun AdminDetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = SlateGrey)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
    }
}
