package com.example.frontend.ui.screens.profile

import com.example.frontend.ui.components.bounceClick
import com.example.frontend.ui.components.ComingSoonDialog
import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.frontend.ui.navigation.Screen
import com.example.frontend.ui.theme.EmeraldPrimary

import com.example.frontend.ui.theme.SlateGrey
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val uiState by viewModel.profileUiState.collectAsState()
    val context = LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    var showComingSoonDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            showEditDialog = false
            viewModel.resetUpdateState()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.resetUpdateState()
        }
    }

    val userName: String = user?.username?.takeIf { it.isNotBlank() } ?: "Guest User"
    val userEmail: String = user?.email?.takeIf { it.isNotBlank() } ?: "guest@velogo.com"
    val userPhone: String = user?.phoneNumber?.takeIf { it.isNotBlank() } ?: "Not Added"
    val userAddress: String = user?.adress?.takeIf { it.isNotBlank() } ?: "Not Added"


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Header
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(EmeraldPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.take(1).uppercase(),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = userName,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = userEmail,
            style = MaterialTheme.typography.bodyMedium,
            color = SlateGrey
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Profile Info Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, SlateGrey.copy(alpha = 0.12f))
        ) {
            Column {
                ProfileItemRow(
                    icon = Icons.Default.Person, 
                    title = "Personal Info", 
                    subtitle = "Manage name, mobile & details",
                    onClick = { showEditDialog = true }
                )
                HorizontalDivider(color = SlateGrey.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))
                ProfileItemRow(icon = Icons.Default.Email, title = "Email Address", subtitle = userEmail)
                HorizontalDivider(color = SlateGrey.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))
                ProfileItemRow(icon = Icons.Default.Phone, title = "Mobile Number", subtitle = userPhone)
                HorizontalDivider(color = SlateGrey.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))
                ProfileItemRow(icon = Icons.Default.Home, title = "Address", subtitle = userAddress)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // App Options
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, SlateGrey.copy(alpha = 0.12f))
        ) {
            Column {
                ProfileItemRow(
                    icon = Icons.Default.Lock, 
                    title = "Change Password", 
                    subtitle = "Secure your login credentials",
                    onClick = { showComingSoonDialog = true }
                )
                HorizontalDivider(color = SlateGrey.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))
                ProfileItemRow(
                    icon = Icons.Default.Lock, 
                    title = "Privacy & Safety", 
                    subtitle = "Account security configuration",
                    onClick = { showComingSoonDialog = true }
                )
                HorizontalDivider(color = SlateGrey.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))
                ProfileItemRow(
                    icon = Icons.Default.Settings, 
                    title = "App Settings", 
                    subtitle = "Theme, units & notifications",
                    onClick = { navController.navigate(Screen.Settings.route) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))


        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        var nameInput by remember { mutableStateOf(userName) }
        var emailInput by remember { mutableStateOf(userEmail) }
        var phoneInput by remember { mutableStateOf(if (userPhone == "Not Added") "" else userPhone) }
        var addressInput by remember { mutableStateOf(if (userAddress == "Not Added") "" else userAddress) }

        AlertDialog(
            onDismissRequest = { if (!uiState.isLoading) showEditDialog = false },
            title = {
                Text(
                    text = "Edit Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    )

                    OutlinedTextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text("Address") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isBlank() || emailInput.isBlank()) {
                            Toast.makeText(context, "Name and Email cannot be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        user?.id?.let { userId ->
                            viewModel.editProfile(
                                userId = userId,
                                username = nameInput,
                                email = emailInput,
                                phoneNumber = phoneInput,
                                adress = addressInput
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false },
                    enabled = !uiState.isLoading
                ) {
                    Text("Cancel", color = SlateGrey)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showComingSoonDialog) {
        ComingSoonDialog(onDismiss = { showComingSoonDialog = false })
    }
}


@Composable
fun ProfileItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val rowModifier = Modifier
        .fillMaxWidth()
        .let {
            if (onClick != null) {
                it.clickable(
                    interactionSource = interactionSource,
                    indication = androidx.compose.foundation.LocalIndication.current,
                    onClick = onClick
                ).bounceClick(interactionSource)
            } else {
                it
            }
        }
        .padding(16.dp)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(EmeraldPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = SlateGrey)
        }

        if (onClick != null) {
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = SlateGrey)
        }
    }
}

