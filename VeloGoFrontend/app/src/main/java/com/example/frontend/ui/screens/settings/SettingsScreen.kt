package com.example.frontend.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.frontend.data.local.pref.AppTheme
import com.example.frontend.ui.components.ComingSoonDialog
import com.example.frontend.ui.navigation.Screen
import com.example.frontend.ui.theme.EmeraldPrimary

import com.example.frontend.ui.theme.SlateGrey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentTheme by viewModel.themeState.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showComingSoonDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                color = EmeraldPrimary,
                fontWeight = FontWeight.Bold
            )

            // Theme Setting Row
            SettingsItemRow(
                icon = Icons.Default.Settings,
                title = "App Theme",
                subtitle = when (currentTheme) {
                    AppTheme.SYSTEM -> "System Default"
                    AppTheme.LIGHT -> "Light Mode"
                    AppTheme.DARK -> "Dark Mode"
                },
                onClick = { showThemeDialog = true }
            )

            HorizontalDivider(color = SlateGrey.copy(alpha = 0.08f))

            Text(
                text = "App Details",
                style = MaterialTheme.typography.titleMedium,
                color = EmeraldPrimary,
                fontWeight = FontWeight.Bold
            )

            SettingsItemRow(
                icon = Icons.Default.Info,
                title = "About App",
                subtitle = "App specs, features & tech stack",
                onClick = { navController.navigate(Screen.About.route) }
            )

            HorizontalDivider(color = SlateGrey.copy(alpha = 0.08f))

            Text(
                text = "Future Expansions",
                style = MaterialTheme.typography.titleMedium,
                color = SlateGrey,
                fontWeight = FontWeight.Bold
            )

            SettingsItemRow(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Manage app alerts and sounds (Coming Soon)",
                onClick = { showComingSoonDialog = true }
            )

            SettingsItemRow(
                icon = Icons.Default.Info,
                title = "Language",
                subtitle = "Select app localization (Coming Soon)",
                onClick = { showComingSoonDialog = true }
            )

        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val themeOptions = listOf(
                        AppTheme.SYSTEM to "System Default",
                        AppTheme.LIGHT to "Light Mode",
                        AppTheme.DARK to "Dark Mode"
                    )
                    themeOptions.forEach { (theme, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setTheme(theme)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (theme == currentTheme),
                                onClick = {
                                    viewModel.setTheme(theme)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
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
fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val modifier = Modifier
        .fillMaxWidth()
        .let {
            if (onClick != null && enabled) {
                it.clickable(
                    interactionSource = interactionSource,
                    indication = androidx.compose.foundation.LocalIndication.current,
                    onClick = onClick
                )
            } else {
                it
            }
        }
        .padding(vertical = 12.dp)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) EmeraldPrimary else SlateGrey.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else SlateGrey.copy(alpha = 0.5f)
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) SlateGrey else SlateGrey.copy(alpha = 0.3f)
            )
        }
    }
}
