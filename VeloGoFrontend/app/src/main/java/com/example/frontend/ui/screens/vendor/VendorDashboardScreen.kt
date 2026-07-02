package com.example.frontend.ui.screens.vendor

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.frontend.domain.model.Vehicle
import com.example.frontend.domain.model.VendorBooking
import com.example.frontend.ui.components.bounceClick
import com.example.frontend.ui.theme.EmeraldPrimary
import com.example.frontend.ui.theme.SlateGrey
import com.example.frontend.ui.util.Resource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDashboardScreen(
    navController: NavController,
    viewModel: VendorViewModel = hiltViewModel(),
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val userSession by viewModel.currentSession.collectAsState()
    val vehiclesState by viewModel.vehiclesState.collectAsState()
    val bookingsState by viewModel.bookingsState.collectAsState()

    val addState by viewModel.addState.collectAsState()
    val editState by viewModel.editState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()
    val changeStatusAction by viewModel.changeStatusState.collectAsState()

    var activeTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingVehicle by remember { mutableStateOf<Vehicle?>(null) }
    var selectedBookingForDetails by remember { mutableStateOf<VendorBooking?>(null) }

    // Toast effects for state changes
    LaunchedEffect(addState) {
        if (addState is Resource.Success && (addState as Resource.Success<String>).data.isNotEmpty()) {
            Toast.makeText(context, (addState as Resource.Success<String>).data, Toast.LENGTH_SHORT).show()
            showAddDialog = false
            viewModel.resetStates()
        } else if (addState is Resource.Error) {
            Toast.makeText(context, (addState as Resource.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetStates()
        }
    }

    LaunchedEffect(editState) {
        if (editState is Resource.Success && (editState as Resource.Success<Vehicle?>).data != null) {
            Toast.makeText(context, "Vehicle updated successfully!", Toast.LENGTH_SHORT).show()
            editingVehicle = null
            viewModel.resetStates()
        } else if (editState is Resource.Error) {
            Toast.makeText(context, (editState as Resource.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetStates()
        }
    }

    LaunchedEffect(deleteState) {
        if (deleteState is Resource.Success && (deleteState as Resource.Success<String>).data.isNotEmpty()) {
            Toast.makeText(context, "Vehicle deleted successfully!", Toast.LENGTH_SHORT).show()
            viewModel.resetStates()
        } else if (deleteState is Resource.Error) {
            Toast.makeText(context, (deleteState as Resource.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetStates()
        }
    }

    LaunchedEffect(changeStatusAction) {
        if (changeStatusAction is Resource.Success && (changeStatusAction as Resource.Success<String>).data.isNotEmpty()) {
            Toast.makeText(context, (changeStatusAction as Resource.Success<String>).data, Toast.LENGTH_SHORT).show()
            viewModel.resetChangeStatusState()
        } else if (changeStatusAction is Resource.Error) {
            Toast.makeText(context, (changeStatusAction as Resource.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetChangeStatusState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Vendor Dashboard", fontWeight = FontWeight.Bold)
                        Text(
                            text = userSession?.email ?: "Vendor Account",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateGrey
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (activeTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Vehicle") },
                    text = { Text("Add Vehicle", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = EmeraldPrimary
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("My Vehicles", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Reservations", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (activeTab) {
                0 -> VehiclesTabContent(
                    state = vehiclesState,
                    onEdit = { editingVehicle = it },
                    onDelete = { viewModel.deleteVehicle(it.id) },
                    onRetry = { viewModel.loadVendorData() }
                )
                1 -> BookingsTabContent(
                    state = bookingsState,
                    onDetails = { selectedBookingForDetails = it },
                    onChangeStatus = { id, status -> viewModel.changeBookingStatus(id, status) },
                    onRetry = { viewModel.loadVendorData() }
                )
            }
        }
    }

    // Dialogs
    if (showAddDialog) {
        AddVehicleDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            isLoading = addState is Resource.Loading
        )
    }

    editingVehicle?.let { vehicle ->
        EditVehicleDialog(
            vehicle = vehicle,
            viewModel = viewModel,
            onDismiss = { editingVehicle = null },
            isLoading = editState is Resource.Loading
        )
    }

    selectedBookingForDetails?.let { booking ->
        BookingDetailsDialog(
            booking = booking,
            onDismiss = { selectedBookingForDetails = null }
        )
    }
}

@Composable
fun VehiclesTabContent(
    state: Resource<List<Vehicle>>,
    onEdit: (Vehicle) -> Unit,
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
            val vehicles = state.data
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
                            text = "No vehicles listed",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Tap Add Vehicle below to submit a listing.",
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
                    items(vehicles) { vehicle ->
                        VehicleItemCard(
                            vehicle = vehicle,
                            onEdit = { onEdit(vehicle) },
                            onDelete = { onDelete(vehicle) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleItemCard(
    vehicle: Vehicle,
    onEdit: () -> Unit,
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
                // Image
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

                // Detail
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

                // Status Badge
                StatusBadge(isAdminApproved = vehicle.isAdminApproved, isRejected = vehicle.isRejected)
            }

            HorizontalDivider(color = SlateGrey.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 12.dp))

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
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Vehicle") },
            text = { Text("Are you sure you want to remove this vehicle listing? This action cannot be undone.") },
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
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = SlateGrey.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No bookings yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Reservations placed on your vehicles will appear here.",
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
                    items(bookings) { booking ->
                        BookingItemCard(
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
fun BookingItemCard(
    booking: VendorBooking,
    onDetails: () -> Unit,
    onChangeStatus: (String) -> Unit
) {
    var statusExpanded by remember { mutableStateOf(false) }
    val statuses = listOf("booked", "onTrip", "tripCompleted", "canceled", "overDue", "notPicked")

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
                // Vehicle image
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
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("PICKUP", style = MaterialTheme.typography.labelSmall, color = SlateGrey)
                    Text(booking.pickUpLocation, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("DROP-OFF", style = MaterialTheme.typography.labelSmall, color = SlateGrey)
                    Text(booking.dropOffLocation, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = SlateGrey.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 12.dp))

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

// Dialog helper to show full booking metadata
@Composable
fun BookingDetailsDialog(
    booking: VendorBooking,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reservation Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailRow(label = "Booking ID", value = booking.id)
                DetailRow(label = "Vehicle", value = "${booking.vehicleDetails.company} ${booking.vehicleDetails.name}")
                DetailRow(label = "Vehicle Reg Number", value = booking.vehicleDetails.registrationNumber)
                DetailRow(label = "Pickup Date", value = booking.pickupDate)
                DetailRow(label = "Drop-off Date", value = booking.dropOffDate)
                DetailRow(label = "Pickup Location", value = booking.pickUpLocation)
                DetailRow(label = "Drop-off Location", value = booking.dropOffLocation)
                DetailRow(label = "Status", value = booking.status.uppercase())
                DetailRow(label = "Total Price", value = "₹${booking.totalPrice}")
                DetailRow(label = "Razorpay Payment ID", value = booking.razorpayPaymentId ?: "N/A")
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
fun AddVehicleDialog(
    viewModel: VendorViewModel,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    val context = LocalContext.current

    val districts by viewModel.districts.collectAsState()
    val locationsMap by viewModel.locationsMap.collectAsState()
    val brands by viewModel.brands.collectAsState()
    val models by viewModel.models.collectAsState()

    var regNum by remember { mutableStateOf("") }
    var selectedCompany by remember { mutableStateOf("") }
    var vehicleName by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("") }
    var carTitle by remember { mutableStateOf("") }
    var basePackage by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var yearStr by remember { mutableStateOf("") }
    var selectedFuel by remember { mutableStateOf("petrol") }
    var selectedSeats by remember { mutableStateOf("5") }
    var selectedTransmission by remember { mutableStateOf("automatic") }
    var selectedCarType by remember { mutableStateOf("sedan") }
    var description by remember { mutableStateOf("") }

    var selectedDistrict by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf("") }

    var insuranceDate by remember { mutableStateOf("") }
    var registrationDate by remember { mutableStateOf("") }
    var pollutionDate by remember { mutableStateOf("") }

    var selectedImageUris by remember { mutableStateOf<List<android.net.Uri>>(emptyList()) }

    // Dropdown state
    var companyExpanded by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }
    var fuelExpanded by remember { mutableStateOf(false) }
    var seatsExpanded by remember { mutableStateOf(false) }
    var transmissionExpanded by remember { mutableStateOf(false) }
    var carTypeExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }
    var locationExpanded by remember { mutableStateOf(false) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris = uris
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Add New Vehicle", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = regNum,
                    onValueChange = { regNum = it },
                    label = { Text("Registration Number*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                // Company dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCompany,
                        onValueChange = {},
                        label = { Text("Company (Brand)*") },
                        trailingIcon = { Text("▼", color = EmeraldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = !isLoading
                    )
                    if (!isLoading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { companyExpanded = true }
                        )
                    }
                    DropdownMenu(
                        expanded = companyExpanded,
                        onDismissRequest = { companyExpanded = false }
                    ) {
                        brands.forEach { brand ->
                            DropdownMenuItem(
                                text = { Text(brand) },
                                onClick = {
                                    selectedCompany = brand
                                    companyExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = vehicleName,
                    onValueChange = { vehicleName = it },
                    label = { Text("Vehicle Name*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                // Model dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedModel,
                        onValueChange = {},
                        label = { Text("Model*") },
                        trailingIcon = { Text("▼", color = EmeraldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = !isLoading
                    )
                    if (!isLoading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { modelExpanded = true }
                        )
                    }
                    DropdownMenu(
                        expanded = modelExpanded,
                        onDismissRequest = { modelExpanded = false }
                    ) {
                        models.forEach { modelName ->
                            DropdownMenuItem(
                                text = { Text(modelName) },
                                onClick = {
                                    selectedModel = modelName
                                    modelExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = carTitle,
                    onValueChange = { carTitle = it },
                    label = { Text("Title Tagline") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = basePackage,
                    onValueChange = { basePackage = it },
                    label = { Text("Base Package (e.g. 50 km)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Price per Day (INR)*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = yearStr,
                    onValueChange = { yearStr = it },
                    label = { Text("Year of Manufacture*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                // Fuel dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedFuel.uppercase(),
                        onValueChange = {},
                        label = { Text("Fuel Type*") },
                        trailingIcon = { Text("▼", color = EmeraldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = !isLoading
                    )
                    if (!isLoading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { fuelExpanded = true }
                        )
                    }
                    DropdownMenu(
                        expanded = fuelExpanded,
                        onDismissRequest = { fuelExpanded = false }
                    ) {
                        listOf("petrol", "diesel", "electirc", "hybrid").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.uppercase()) },
                                onClick = {
                                    selectedFuel = type
                                    fuelExpanded = false
                                }
                            )
                        }
                    }
                }

                // Car Type dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCarType.uppercase(),
                        onValueChange = {},
                        label = { Text("Car Type*") },
                        trailingIcon = { Text("▼", color = EmeraldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = !isLoading
                    )
                    if (!isLoading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { carTypeExpanded = true }
                        )
                    }
                    DropdownMenu(
                        expanded = carTypeExpanded,
                        onDismissRequest = { carTypeExpanded = false }
                    ) {
                        listOf("sedan", "suv", "hatchback").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.uppercase()) },
                                onClick = {
                                    selectedCarType = type
                                    carTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Seats dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedSeats,
                        onValueChange = {},
                        label = { Text("Seats Capacity*") },
                        trailingIcon = { Text("▼", color = EmeraldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = !isLoading
                    )
                    if (!isLoading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { seatsExpanded = true }
                        )
                    }
                    DropdownMenu(
                        expanded = seatsExpanded,
                        onDismissRequest = { seatsExpanded = false }
                    ) {
                        listOf("5", "7", "8").forEach { count ->
                            DropdownMenuItem(
                                text = { Text(count) },
                                onClick = {
                                    selectedSeats = count
                                    seatsExpanded = false
                                }
                            )
                        }
                    }
                }

                // Transmission dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedTransmission.uppercase(),
                        onValueChange = {},
                        label = { Text("Transmission*") },
                        trailingIcon = { Text("▼", color = EmeraldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = !isLoading
                    )
                    if (!isLoading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { transmissionExpanded = true }
                        )
                    }
                    DropdownMenu(
                        expanded = transmissionExpanded,
                        onDismissRequest = { transmissionExpanded = false }
                    ) {
                        listOf("automatic", "manual").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.uppercase()) },
                                onClick = {
                                    selectedTransmission = type
                                    transmissionExpanded = false
                                }
                            )
                        }
                    }
                }

                // District dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedDistrict,
                        onValueChange = {},
                        label = { Text("District*") },
                        trailingIcon = { Text("▼", color = EmeraldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = !isLoading
                    )
                    if (!isLoading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { districtExpanded = true }
                        )
                    }
                    DropdownMenu(
                        expanded = districtExpanded,
                        onDismissRequest = { districtExpanded = false }
                    ) {
                        districts.forEach { dist ->
                            DropdownMenuItem(
                                text = { Text(dist) },
                                onClick = {
                                    selectedDistrict = dist
                                    selectedLocation = ""
                                    districtExpanded = false
                                }
                            )
                        }
                    }
                }

                // Location dropdown
                val locations = locationsMap[selectedDistrict] ?: emptyList()
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedLocation,
                        onValueChange = {},
                        label = { Text("Location*") },
                        trailingIcon = { Text("▼", color = EmeraldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = !isLoading && selectedDistrict.isNotEmpty()
                    )
                    if (!isLoading && selectedDistrict.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { locationExpanded = true }
                        )
                    }
                    DropdownMenu(
                        expanded = locationExpanded,
                        onDismissRequest = { locationExpanded = false }
                    ) {
                        locations.forEach { loc ->
                            DropdownMenuItem(
                                text = { Text(loc) },
                                onClick = {
                                    selectedLocation = loc
                                    locationExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Vehicle Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                // Date Pickers
                DatePickerField(label = "Insurance End Date*", date = insuranceDate, onDateSelected = { insuranceDate = it }, enabled = !isLoading)
                DatePickerField(label = "Registration End Date*", date = registrationDate, onDateSelected = { registrationDate = it }, enabled = !isLoading)
                DatePickerField(label = "Pollution End Date*", date = pollutionDate, onDateSelected = { pollutionDate = it }, enabled = !isLoading)

                Spacer(modifier = Modifier.height(8.dp))

                // Multiple Images Selection
                Text("Select Images*", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    enabled = !isLoading
                ) {
                    Text("Select Photos", color = Color.Black)
                }

                if (selectedImageUris.isNotEmpty()) {
                    Text("${selectedImageUris.size} photos selected", fontSize = 12.sp, color = EmeraldPrimary)
                    // Grid list preview
                    Box(modifier = Modifier.height(100.dp).fillMaxWidth()) {
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(selectedImageUris) { uri ->
                                Image(
                                    painter = rememberAsyncImagePainter(uri),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (regNum.isBlank() || selectedCompany.isBlank() || vehicleName.isBlank() ||
                        selectedModel.isBlank() || priceStr.isBlank() || yearStr.isBlank() ||
                        selectedDistrict.isBlank() || selectedLocation.isBlank() ||
                        insuranceDate.isBlank() || registrationDate.isBlank() ||
                        pollutionDate.isBlank() || selectedImageUris.isEmpty()
                    ) {
                        Toast.makeText(context, "Please fill in all required fields and pick images", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Map selected URIs to Name + Bytes
                    val imagesData = selectedImageUris.mapNotNull { uri ->
                        getUriNameAndBytes(context, uri)
                    }

                    if (imagesData.isEmpty()) {
                        Toast.makeText(context, "Failed to load selected images", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.addVehicle(
                        registerationNumber = regNum,
                        company = selectedCompany,
                        name = vehicleName,
                        model = selectedModel,
                        title = carTitle,
                        basePackage = basePackage,
                        price = priceStr.toDoubleOrNull() ?: 0.0,
                        yearMade = yearStr.toIntOrNull() ?: 0,
                        fuelType = selectedFuel,
                        description = description,
                        seat = selectedSeats.toIntOrNull() ?: 5,
                        transmitionType = selectedTransmission,
                        insuranceEndDate = insuranceDate,
                        registrationEndDate = registrationDate,
                        pollutionEndDate = pollutionDate,
                        carType = selectedCarType,
                        location = selectedLocation,
                        district = selectedDistrict,
                        images = imagesData
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Submit", color = Color.Black)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel", color = SlateGrey)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun EditVehicleDialog(
    vehicle: Vehicle,
    viewModel: VendorViewModel,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    val context = LocalContext.current

    val districts by viewModel.districts.collectAsState()
    val locationsMap by viewModel.locationsMap.collectAsState()
    val brands by viewModel.brands.collectAsState()
    val models by viewModel.models.collectAsState()

    var regNum by remember { mutableStateOf(vehicle.registrationNumber) }
    var selectedCompany by remember { mutableStateOf(vehicle.company ?: "") }
    var vehicleName by remember { mutableStateOf(vehicle.name ?: "") }
    var selectedModel by remember { mutableStateOf(vehicle.model ?: "") }
    var carTitle by remember { mutableStateOf(vehicle.carTitle ?: "") }
    var basePackage by remember { mutableStateOf(vehicle.basePackage ?: "") }
    var priceStr by remember { mutableStateOf(vehicle.price.toString()) }
    var yearStr by remember { mutableStateOf(vehicle.yearMade?.toString() ?: "") }
    var selectedFuel by remember { mutableStateOf(vehicle.fuelType ?: "petrol") }
    var selectedSeats by remember { mutableStateOf(vehicle.seats?.toString() ?: "5") }
    var selectedTransmission by remember { mutableStateOf(vehicle.transmission ?: "automatic") }
    var selectedCarType by remember { mutableStateOf(vehicle.carType ?: "sedan") }
    var description by remember { mutableStateOf(vehicle.carDescription ?: "") }

    var selectedDistrict by remember { mutableStateOf(vehicle.district) }
    var selectedLocation by remember { mutableStateOf(vehicle.location) }

    // Date parsing/formating helper
    val parseDate: (String?) -> String = { raw ->
        if (raw.isNullOrEmpty()) "" else {
            try {
                // Backend date format standard ISO (e.g. 2026-06-25T20:44:01Z)
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val parsed = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).parse(raw)
                format.format(parsed!!)
            } catch (e: Exception) {
                try {
                    val parsed = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse(raw)
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(parsed!!)
                } catch (e2: Exception) {
                    raw.substringBefore("T")
                }
            }
        }
    }

    var insuranceDate by remember { mutableStateOf(parseDate(vehicle.insuranceEnd)) }
    var registrationDate by remember { mutableStateOf(parseDate(vehicle.registrationEnd)) }
    var pollutionDate by remember { mutableStateOf(parseDate(vehicle.pollutionEnd)) }

    // Dropdown state
    var companyExpanded by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }
    var fuelExpanded by remember { mutableStateOf(false) }
    var seatsExpanded by remember { mutableStateOf(false) }
    var transmissionExpanded by remember { mutableStateOf(false) }
    var carTypeExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }
    var locationExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Edit Vehicle Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = regNum,
                    onValueChange = { regNum = it },
                    label = { Text("Registration Number*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                // Company dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCompany,
                        onValueChange = {},
                        label = { Text("Company (Brand)*") },
                        trailingIcon = { Text("▼", color = EmeraldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = !isLoading
                    )
                    if (!isLoading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { companyExpanded = true }
                        )
                    }
                    DropdownMenu(
                        expanded = companyExpanded,
                        onDismissRequest = { companyExpanded = false }
                    ) {
                        brands.forEach { brand ->
                            DropdownMenuItem(
                                text = { Text(brand) },
                                onClick = {
                                    selectedCompany = brand
                                    companyExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = vehicleName,
                    onValueChange = { vehicleName = it },
                    label = { Text("Vehicle Name*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                // Model dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedModel,
                        onValueChange = {},
                        label = { Text("Model*") },
                        trailingIcon = { Text("▼", color = EmeraldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = !isLoading
                    )
                    if (!isLoading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { modelExpanded = true }
                        )
                    }
                    DropdownMenu(
                        expanded = modelExpanded,
                        onDismissRequest = { modelExpanded = false }
                    ) {
                        models.forEach { modelName ->
                            DropdownMenuItem(
                                text = { Text(modelName) },
                                onClick = {
                                    selectedModel = modelName
                                    modelExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = carTitle,
                    onValueChange = { carTitle = it },
                    label = { Text("Title Tagline") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = basePackage,
                    onValueChange = { basePackage = it },
                    label = { Text("Base Package (e.g. 50 km)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Price per Day (INR)*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = yearStr,
                    onValueChange = { yearStr = it },
                    label = { Text("Year of Manufacture*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                // Fuel dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedFuel.uppercase(),
                        onValueChange = {},
                        label = { Text("Fuel Type*") },
                        trailingIcon = { Text("▼", color = EmeraldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = !isLoading
                    )
                    if (!isLoading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { fuelExpanded = true }
                        )
                    }
                    DropdownMenu(
                        expanded = fuelExpanded,
                        onDismissRequest = { fuelExpanded = false }
                    ) {
                        listOf("petrol", "diesel", "electirc", "hybrid").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.uppercase()) },
                                onClick = {
                                    selectedFuel = type
                                    fuelExpanded = false
                                }
                            )
                        }
                    }
                }

                // Car Type dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCarType.uppercase(),
                        onValueChange = {},
                        label = { Text("Car Type*") },
                        trailingIcon = { Text("▼", color = EmeraldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = !isLoading
                    )
                    if (!isLoading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { carTypeExpanded = true }
                        )
                    }
                    DropdownMenu(
                        expanded = carTypeExpanded,
                        onDismissRequest = { carTypeExpanded = false }
                    ) {
                        listOf("sedan", "suv", "hatchback").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.uppercase()) },
                                onClick = {
                                    selectedCarType = type
                                    carTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Seats dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedSeats,
                        onValueChange = {},
                        label = { Text("Seats Capacity*") },
                        trailingIcon = { Text("▼", color = EmeraldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = !isLoading
                    )
                    if (!isLoading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { seatsExpanded = true }
                        )
                    }
                    DropdownMenu(
                        expanded = seatsExpanded,
                        onDismissRequest = { seatsExpanded = false }
                    ) {
                        listOf("5", "7", "8").forEach { count ->
                            DropdownMenuItem(
                                text = { Text(count) },
                                onClick = {
                                    selectedSeats = count
                                    seatsExpanded = false
                                }
                            )
                        }
                    }
                }

                // Transmission dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedTransmission.uppercase(),
                        onValueChange = {},
                        label = { Text("Transmission*") },
                        trailingIcon = { Text("▼", color = EmeraldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = !isLoading
                    )
                    if (!isLoading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { transmissionExpanded = true }
                        )
                    }
                    DropdownMenu(
                        expanded = transmissionExpanded,
                        onDismissRequest = { transmissionExpanded = false }
                    ) {
                        listOf("automatic", "manual").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.uppercase()) },
                                onClick = {
                                    selectedTransmission = type
                                    transmissionExpanded = false
                                }
                            )
                        }
                    }
                }

                // District dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedDistrict,
                        onValueChange = {},
                        label = { Text("District*") },
                        trailingIcon = { Text("▼", color = EmeraldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = !isLoading
                    )
                    if (!isLoading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { districtExpanded = true }
                        )
                    }
                    DropdownMenu(
                        expanded = districtExpanded,
                        onDismissRequest = { districtExpanded = false }
                    ) {
                        districts.forEach { dist ->
                            DropdownMenuItem(
                                text = { Text(dist) },
                                onClick = {
                                    selectedDistrict = dist
                                    selectedLocation = ""
                                    districtExpanded = false
                                }
                            )
                        }
                    }
                }

                // Location dropdown
                val locations = locationsMap[selectedDistrict] ?: emptyList()
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedLocation,
                        onValueChange = {},
                        label = { Text("Location*") },
                        trailingIcon = { Text("▼", color = EmeraldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = !isLoading && selectedDistrict.isNotEmpty()
                    )
                    if (!isLoading && selectedDistrict.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { locationExpanded = true }
                        )
                    }
                    DropdownMenu(
                        expanded = locationExpanded,
                        onDismissRequest = { locationExpanded = false }
                    ) {
                        locations.forEach { loc ->
                            DropdownMenuItem(
                                text = { Text(loc) },
                                onClick = {
                                    selectedLocation = loc
                                    locationExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Vehicle Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                // Date fields
                DatePickerField(label = "Insurance End Date", date = insuranceDate, onDateSelected = { insuranceDate = it }, enabled = !isLoading)
                DatePickerField(label = "Registration End Date", date = registrationDate, onDateSelected = { registrationDate = it }, enabled = !isLoading)
                DatePickerField(label = "Pollution End Date", date = pollutionDate, onDateSelected = { pollutionDate = it }, enabled = !isLoading)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (regNum.isBlank() || selectedCompany.isBlank() || vehicleName.isBlank() ||
                        selectedModel.isBlank() || priceStr.isBlank() || yearStr.isBlank() ||
                        selectedDistrict.isBlank() || selectedLocation.isBlank() ||
                        insuranceDate.isBlank() || registrationDate.isBlank() ||
                        pollutionDate.isBlank()
                    ) {
                        Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.editVehicle(
                        vehicleId = vehicle.id,
                        registerationNumber = regNum,
                        company = selectedCompany,
                        name = vehicleName,
                        model = selectedModel,
                        title = carTitle,
                        basePackage = basePackage,
                        price = priceStr.toDoubleOrNull() ?: 0.0,
                        yearMade = yearStr.toIntOrNull() ?: 0,
                        description = description,
                        seats = selectedSeats.toIntOrNull() ?: 5,
                        transmitionType = selectedTransmission,
                        registrationEndDate = registrationDate,
                        insuranceEndDate = insuranceDate,
                        pollutionEndDate = pollutionDate,
                        carType = selectedCarType,
                        fuelType = selectedFuel,
                        location = selectedLocation,
                        district = selectedDistrict
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save", color = Color.Black)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel", color = SlateGrey)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun DatePickerField(
    label: String,
    date: String,
    onDateSelected: (String) -> Unit,
    enabled: Boolean
) {
    val context = LocalContext.current
    OutlinedTextField(
        value = date,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        trailingIcon = {
            IconButton(
                onClick = {
                    if (enabled) {
                        val calendar = Calendar.getInstance()
                        android.app.DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedCalendar = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                }
                                val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedCalendar.time)
                                onDateSelected(formatted)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                },
                enabled = enabled
            ) {
                Icon(Icons.Default.DateRange, contentDescription = "Pick Date", tint = EmeraldPrimary)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    )
}

@Composable
fun StatusBadge(isAdminApproved: Boolean, isRejected: Boolean) {
    val bgColor = when {
        isRejected -> Color(0xFFFFEBEE)
        isAdminApproved -> Color(0xFFE8F5E9)
        else -> Color(0xFFFFFDE7)
    }
    val txtColor = when {
        isRejected -> Color(0xFFC62828)
        isAdminApproved -> Color(0xFF2E7D32)
        else -> Color(0xFFF57F17)
    }
    val text = when {
        isRejected -> "Rejected"
        isAdminApproved -> "Approved"
        else -> "Pending"
    }

    Box(
        modifier = Modifier
            .background(bgColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = txtColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = SlateGrey)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
    }
}

// Memory reader helpers
private fun getUriNameAndBytes(context: android.content.Context, uri: android.net.Uri): Pair<String, ByteArray>? {
    val contentResolver = context.contentResolver
    val cursor = contentResolver.query(uri, null, null, null, null)
    val name = cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) it.getString(nameIndex) else null
        } else null
    } ?: uri.lastPathSegment ?: "image_${System.currentTimeMillis()}.jpg"

    return try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val bytes = inputStream.readBytes()
            Pair(name, bytes)
        }
    } catch (e: Exception) {
        null
    }
}
