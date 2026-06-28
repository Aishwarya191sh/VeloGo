package com.example.frontend.ui.screens.home

import android.widget.Toast
import com.example.frontend.ui.components.bounceClick
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.frontend.R
import com.example.frontend.ui.components.RatingBar
import com.example.frontend.ui.components.RentARideButton
import com.example.frontend.ui.components.RentARideDatePicker
import com.example.frontend.ui.components.RentARideTimePicker
import com.example.frontend.ui.navigation.Screen
import com.example.frontend.ui.theme.EmeraldPrimary
import com.example.frontend.ui.theme.EmeraldPrimaryContainer
import com.example.frontend.ui.theme.SlateGrey
import java.util.*

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: com.example.frontend.ui.screens.search.SearchFlowViewModel,
    username: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // District and Location Dropdown control
    var showDistrictMenu by remember { mutableStateOf(false) }
    var showPickUpMenu by remember { mutableStateOf(false) }
    var showDropOffMenu by remember { mutableStateOf(false) }


    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Welcome and Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                EmeraldPrimary.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.CenterStart)) {
                    val greetingName = username?.takeIf { it.isNotBlank() } ?: "User"
                    Text(
                        text = "Hello, $greetingName 👋",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Find the perfect car for your next adventure.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SlateGrey
                    )
                }
            }
        }

        // Search Widget
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-40).dp)
                    .shadow(12.dp, shape = RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Book Your Ride",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    // District Selector
                    Box {
                        OutlinedButton(
                            onClick = { showDistrictMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = EmeraldPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text("Select District", style = MaterialTheme.typography.labelSmall, color = SlateGrey)
                                        Text(
                                            text = uiState.selectedDistrict.ifEmpty { "Select District" },
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SlateGrey)
                            }
                        }
                        DropdownMenu(
                            expanded = showDistrictMenu,
                            onDismissRequest = { showDistrictMenu = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            if (uiState.isLoadingLocations) {
                                DropdownMenuItem(
                                    text = { Text("Loading districts...") },
                                    onClick = {}
                                )
                            } else {
                                uiState.districts.forEach { dist ->
                                    DropdownMenuItem(
                                        text = { Text(dist) },
                                        onClick = {
                                            viewModel.setDistrict(dist)
                                            showDistrictMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Pick-up Location Selector
                    Box {
                        OutlinedButton(
                            onClick = { showPickUpMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = EmeraldPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text("Pick-up Location", style = MaterialTheme.typography.labelSmall, color = SlateGrey)
                                        Text(
                                            text = uiState.pickupLocation.ifEmpty { "Select Pick-up Location" },
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SlateGrey)
                            }
                        }
                        DropdownMenu(
                            expanded = showPickUpMenu,
                            onDismissRequest = { showPickUpMenu = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            val pickUpLocs = uiState.locationsMap[uiState.selectedDistrict] ?: emptyList()
                            if (pickUpLocs.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No locations found") },
                                    onClick = {}
                                )
                            } else {
                                pickUpLocs.forEach { loc ->
                                    DropdownMenuItem(
                                        text = { Text(loc) },
                                        onClick = {
                                            viewModel.setPickupLocation(loc)
                                            showPickUpMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Drop-off Location Selector
                    Box {
                        OutlinedButton(
                            onClick = { showDropOffMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = EmeraldPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text("Drop-off Location", style = MaterialTheme.typography.labelSmall, color = SlateGrey)
                                        Text(
                                            text = uiState.dropoffLocation.ifEmpty { "Select Drop-off Location" },
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SlateGrey)
                            }
                        }
                        DropdownMenu(
                            expanded = showDropOffMenu,
                            onDismissRequest = { showDropOffMenu = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            val dropOffLocs = uiState.locationsMap[uiState.selectedDistrict] ?: emptyList()
                            if (dropOffLocs.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No locations found") },
                                    onClick = {}
                                )
                            } else {
                                dropOffLocs.forEach { loc ->
                                    DropdownMenuItem(
                                        text = { Text(loc) },
                                        onClick = {
                                            viewModel.setDropoffLocation(loc)
                                            showDropOffMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Dates and Times
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            RentARideDatePicker(
                                label = "Pick-up Date",
                                selectedDate = uiState.pickupDateTime,
                                onDateSelected = { newDate ->
                                    val updated = (uiState.pickupDateTime ?: Calendar.getInstance()).apply {
                                        set(Calendar.YEAR, newDate.get(Calendar.YEAR))
                                        set(Calendar.MONTH, newDate.get(Calendar.MONTH))
                                        set(Calendar.DAY_OF_MONTH, newDate.get(Calendar.DAY_OF_MONTH))
                                    }
                                    viewModel.setPickupDateTime(updated)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            RentARideTimePicker(
                                label = "Pick-up Time",
                                selectedTime = uiState.pickupDateTime,
                                onTimeSelected = { h, m ->
                                    val updated = (uiState.pickupDateTime ?: Calendar.getInstance()).apply {
                                        set(Calendar.HOUR_OF_DAY, h)
                                        set(Calendar.MINUTE, m)
                                    }
                                    viewModel.setPickupDateTime(updated)
                                }
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            RentARideDatePicker(
                                label = "Drop-off Date",
                                selectedDate = uiState.dropoffDateTime,
                                onDateSelected = { newDate ->
                                    val updated = (uiState.dropoffDateTime ?: Calendar.getInstance()).apply {
                                        set(Calendar.YEAR, newDate.get(Calendar.YEAR))
                                        set(Calendar.MONTH, newDate.get(Calendar.MONTH))
                                        set(Calendar.DAY_OF_MONTH, newDate.get(Calendar.DAY_OF_MONTH))
                                    }
                                    viewModel.setDropoffDateTime(updated)
                                },
                                minDate = uiState.pickupDateTime ?: Calendar.getInstance()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            RentARideTimePicker(
                                label = "Drop-off Time",
                                selectedTime = uiState.dropoffDateTime,
                                onTimeSelected = { h, m ->
                                    val updated = (uiState.dropoffDateTime ?: Calendar.getInstance()).apply {
                                        set(Calendar.HOUR_OF_DAY, h)
                                        set(Calendar.MINUTE, m)
                                    }
                                    viewModel.setDropoffDateTime(updated)
                                }
                            )
                        }
                    }

                    // Display validation error if present
                    if (uiState.searchError != null) {
                        Text(
                            text = uiState.searchError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Search Button
                    RentARideButton(
                        text = "Search Available Cars",
                        onClick = {
                            if (uiState.pickupDateTime == null || uiState.dropoffDateTime == null) {
                                Toast.makeText(context, "Please select pick-up and drop-off dates", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.searchVehicles {
                                    navController.navigate(Screen.SearchResults.route)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = EmeraldPrimary,
                        contentColor = Color.Black,
                        isLoading = uiState.isSearching
                    )
                }
            }
        }

    }
}
