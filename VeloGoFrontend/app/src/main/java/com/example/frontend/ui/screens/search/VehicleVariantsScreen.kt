package com.example.frontend.ui.screens.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.frontend.R
import com.example.frontend.domain.model.SearchFilters
import com.example.frontend.domain.model.SortOption
import com.example.frontend.domain.model.Vehicle
import com.example.frontend.ui.components.EmptyOrErrorStateView
import com.example.frontend.ui.components.RentARideButton
import com.example.frontend.ui.components.bounceClick
import com.example.frontend.ui.navigation.Screen
import com.example.frontend.ui.theme.EmeraldPrimary
import com.example.frontend.ui.theme.EmeraldPrimaryContainer
import com.example.frontend.ui.theme.SlateGrey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleVariantsScreen(
    navController: NavController,
    viewModel: SearchFlowViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.selectedModel?.name ?: "Available Variants",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Select specific trim & package",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateGrey
                )
            }
            IconButton(onClick = { showFilterSheet = true }) {
                BadgedBox(
                    badge = {
                        val activeFilterCount = (if (uiState.activeFilters.carTypes.isNotEmpty()) 1 else 0) +
                                (if (uiState.activeFilters.transmissions.isNotEmpty()) 1 else 0) +
                                (if (uiState.activeFilters.fuelTypes.isNotEmpty()) 1 else 0) +
                                (if (uiState.activeFilters.seats.isNotEmpty()) 1 else 0)
                        if (activeFilterCount > 0) {
                            Badge(containerColor = EmeraldPrimary) {
                                Text(activeFilterCount.toString(), color = Color.Black)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Filter variants",
                        tint = if (uiState.activeFilters != SearchFilters()) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Quick Sort Options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = {
                    viewModel.updateSort(
                        if (uiState.activeSort == SortOption.PRICE_LOW_TO_HIGH) SortOption.NONE else SortOption.PRICE_LOW_TO_HIGH
                    )
                },
                label = { Text("Price: Low to High") },
                leadingIcon = {
                    if (uiState.activeSort == SortOption.PRICE_LOW_TO_HIGH) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                },
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = if (uiState.activeSort == SortOption.PRICE_LOW_TO_HIGH) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                )
            )

            AssistChip(
                onClick = {
                    viewModel.updateSort(
                        if (uiState.activeSort == SortOption.PRICE_HIGH_TO_LOW) SortOption.NONE else SortOption.PRICE_HIGH_TO_LOW
                    )
                },
                label = { Text("Price: High to Low") },
                leadingIcon = {
                    if (uiState.activeSort == SortOption.PRICE_HIGH_TO_LOW) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                },
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = if (uiState.activeSort == SortOption.PRICE_HIGH_TO_LOW) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                )
            )
        }

        // Variants List
        if (uiState.isLoadingVariants) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
        } else if (uiState.variantsError != null) {
            EmptyOrErrorStateView(
                title = "Error Loading Variants",
                message = uiState.variantsError ?: "An unexpected error occurred",
                actionButtonText = "Retry",
                onActionClick = { viewModel.fetchVariantsForSelectedModel() }
            )
        } else if (uiState.filteredVariants.isEmpty()) {
            EmptyOrErrorStateView(
                title = "No Variants Found",
                message = "No specific cars match the selected filters. Clear filters and try again.",
                actionButtonText = "Clear Filters",
                onActionClick = { viewModel.updateFilters(SearchFilters()) }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(uiState.filteredVariants) { vehicle ->
                    VariantCard(vehicle = vehicle) {
                        viewModel.selectVariant(vehicle)
                        navController.navigate(Screen.VehicleDetails.createRoute(vehicle.id))
                    }
                }
            }
        }

        // Filter Sheet
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                FilterSheetContent(
                    filters = uiState.activeFilters,
                    onApply = { updatedFilters ->
                        viewModel.updateFilters(updatedFilters)
                        showFilterSheet = false
                    },
                    onReset = {
                        viewModel.updateFilters(SearchFilters())
                        showFilterSheet = false
                    }
                )
            }
        }
    }
}

@Composable
fun VariantCard(
    vehicle: Vehicle,
    onBookClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SlateGrey.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top aspect card image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark) Color(0xFF1E2621) else Color(0xFFE8F5E9)),
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
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.empty_vehicle),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Brand & Price per Day
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = vehicle.name ?: "Unknown Variant",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${vehicle.company ?: "VeloGo"} • ${vehicle.registrationNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateGrey
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${vehicle.price.toInt()}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    )
                    Text(
                        text = "Per Day",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = SlateGrey.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(12.dp))

            // Specs grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpecIconText(icon = Icons.Default.Face, text = "${vehicle.seats ?: 5} Seats")
                SpecIconText(icon = Icons.Default.Settings, text = vehicle.transmission?.capitalize() ?: "Automatic")
                SpecIconText(icon = Icons.Default.Info, text = vehicle.fuelType?.capitalize() ?: "Petrol")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Book Ride CTA button
            RentARideButton(
                text = "Book Ride",
                onClick = onBookClick,
                containerColor = EmeraldPrimary,
                contentColor = Color.Black,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SpecIconText(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SlateGrey,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = SlateGrey
        )
    }
}

@Composable
fun FilterSheetContent(
    filters: SearchFilters,
    onApply: (SearchFilters) -> Unit,
    onReset: () -> Unit
) {
    var selectedTransmissions by remember { mutableStateOf(filters.transmissions) }
    var selectedCarTypes by remember { mutableStateOf(filters.carTypes) }
    var selectedFuelTypes by remember { mutableStateOf(filters.fuelTypes) }
    var selectedSeats by remember { mutableStateOf(filters.seats) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filter Variants", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text(
                "Reset",
                color = EmeraldPrimary,
                modifier = Modifier.clickable { onReset() },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Transmission Filters
        Text("Transmission", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("manual", "automatic").forEach { trans ->
                FilterChip(
                    selected = selectedTransmissions.contains(trans),
                    onClick = {
                        selectedTransmissions = if (selectedTransmissions.contains(trans)) {
                            selectedTransmissions - trans
                        } else {
                            selectedTransmissions + trans
                        }
                    },
                    label = { Text(trans.capitalize()) }
                )
            }
        }

        // Car Type Filters
        Text("Car Type", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 8.dp
        ) {
            listOf("SUV", "Sedan", "Hatchback", "Luxury").forEach { type ->
                FilterChip(
                    selected = selectedCarTypes.contains(type),
                    onClick = {
                        selectedCarTypes = if (selectedCarTypes.contains(type)) {
                            selectedCarTypes - type
                        } else {
                            selectedCarTypes + type
                        }
                    },
                    label = { Text(type) }
                )
            }
        }

        // Fuel Type Filters
        Text("Fuel Type", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("petrol", "diesel", "electric", "hybrid").forEach { fuel ->
                FilterChip(
                    selected = selectedFuelTypes.contains(fuel),
                    onClick = {
                        selectedFuelTypes = if (selectedFuelTypes.contains(fuel)) {
                            selectedFuelTypes - fuel
                        } else {
                            selectedFuelTypes + fuel
                        }
                    },
                    label = { Text(fuel.capitalize()) }
                )
            }
        }

        // Seats Filters
        Text("Seats", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(4, 5, 7).forEach { seat ->
                FilterChip(
                    selected = selectedSeats.contains(seat),
                    onClick = {
                        selectedSeats = if (selectedSeats.contains(seat)) {
                            selectedSeats - seat
                        } else {
                            selectedSeats + seat
                        }
                    },
                    label = { Text("$seat Seats") }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        RentARideButton(
            text = "Apply Filters",
            onClick = {
                onApply(
                    SearchFilters(
                        transmissions = selectedTransmissions,
                        carTypes = selectedCarTypes,
                        fuelTypes = selectedFuelTypes,
                        seats = selectedSeats
                    )
                )
            },
            containerColor = EmeraldPrimary,
            contentColor = Color.Black
        )
    }
}

// Simple FlowRow helper for layout alignment
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val layoutWidth = constraints.maxWidth
        val lines = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentLine = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentLineWidth = 0

        placeables.forEach { placeable ->
            if (currentLineWidth + placeable.width + mainAxisSpacing.roundToPx() > layoutWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = mutableListOf()
                currentLineWidth = 0
            }
            currentLine.add(placeable)
            currentLineWidth += placeable.width + mainAxisSpacing.roundToPx()
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        var totalHeight = 0
        lines.forEachIndexed { index, line ->
            val lineHeight = line.maxOf { it.height }
            totalHeight += lineHeight
            if (index < lines.size - 1) {
                totalHeight += crossAxisSpacing.roundToPx()
            }
        }

        layout(layoutWidth, totalHeight) {
            var y = 0
            lines.forEach { line ->
                val lineHeight = line.maxOf { it.height }
                var x = 0
                line.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + mainAxisSpacing.roundToPx()
                }
                y += lineHeight + crossAxisSpacing.roundToPx()
            }
        }
    }
}
