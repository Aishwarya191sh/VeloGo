package com.example.frontend.ui.screens.booking

import androidx.activity.ComponentActivity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.frontend.R
import com.example.frontend.ui.components.RentARideButton
import com.example.frontend.ui.theme.EmeraldPrimary
import com.example.frontend.ui.theme.EmeraldPrimaryContainer
import com.example.frontend.ui.theme.OnEmeraldPrimaryContainer
import com.example.frontend.ui.theme.SlateGrey

@Composable
fun BookingSuccessScreen(
    navController: NavController,
    viewModel: CheckoutViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
) {
    val uiState by viewModel.uiState.collectAsState()
    val details = uiState.bookingDetails

    var animateTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateTrigger = true
    }

    val checkmarkScale by animateFloatAsState(
        targetValue = if (animateTrigger) 1.1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "success_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Animated Success Circle
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(checkmarkScale)
                .clip(CircleShape)
                .background(EmeraldPrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success checkmark",
                tint = OnEmeraldPrimaryContainer,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Booking Confirmed!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Thank you for riding with VeloGo. Your payment has been received and booking has been processed successfully.",
            style = MaterialTheme.typography.bodyMedium,
            color = SlateGrey,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Booking details card
        if (details != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SlateGrey.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Booking Receipt",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Reference ID", style = MaterialTheme.typography.bodyMedium, color = SlateGrey)
                        Text(
                            text = details.booking.id,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Vehicle Model", style = MaterialTheme.typography.bodyMedium, color = SlateGrey)
                        Text(
                            text = details.vehicle.name ?: "Premium Vehicle",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Pick-up Location", style = MaterialTheme.typography.bodyMedium, color = SlateGrey)
                        Text(
                            text = details.booking.pickUpLocation,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Charged", style = MaterialTheme.typography.bodyMedium, color = SlateGrey)
                        Text(
                            text = "₹${details.booking.totalPrice.toInt()}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = SlateGrey.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "A confirmation email containing your digital receipt and trip policy has been sent to ${uiState.email}.",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateGrey,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        RentARideButton(
            text = "Back to Dashboard",
            onClick = {
                navController.navigate("home") {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier
                .width(220.dp)
                .widthIn(max = 500.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}
