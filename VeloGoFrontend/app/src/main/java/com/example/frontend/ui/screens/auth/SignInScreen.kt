package com.example.frontend.ui.screens.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.R
import com.example.frontend.ui.components.GoogleSignInButton
import com.example.frontend.ui.components.RentARideButton
import com.example.frontend.ui.components.RentARideTextField
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun SignInScreen(
    viewModel: AuthViewModel,
    onNavigateToUser: () -> Unit,
    onNavigateToVendor: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToVendorSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Emerald Green Customer Accent Color
    val accentColor = Color(0xFF00C853)

    val isDark = isSystemInDarkTheme()

    // Highly subtle and elegant background gradient
    val bgGradient = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(Color(0xFF0A0A0A), Color(0xFF0B0F0C))
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(Color(0xFFF6F8F6), Color(0xFFF0F4F1))
            )
        }
    }

    // Fluid Adaptive Sizing Calculations
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val heroImageHeight = (screenHeight * 0.16f).coerceIn(80.dp, 130.dp)
    val spacingHeight = (screenHeight * 0.03f).coerceIn(8.dp, 24.dp)
    val headerSpacing = (screenHeight * 0.02f).coerceIn(6.dp, 16.dp)
    val cardPadding = (screenHeight * 0.03f).coerceIn(12.dp, 24.dp)
    val innerSpacing = (screenHeight * 0.025f).coerceIn(12.dp, 20.dp)
    val bottomSpacing = (screenHeight * 0.035f).coerceIn(16.dp, 28.dp)

    // Scale font size continuously based on screen height
    val titleFontSize = (screenHeight.value * 0.045f).coerceIn(24f, 30f).sp
    val titleLineHeight = (screenHeight.value * 0.055f).coerceIn(30f, 36f).sp

    // Google Sign-In Setup
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val name = account?.displayName ?: ""
            val userEmail = account?.email ?: ""
            val photo = account?.photoUrl?.toString() ?: ""

            viewModel.signInWithGoogle(name, userEmail, photo, isVendor = false)
        } catch (e: Exception) {
            // Handle Google login cancel or error
        }
    }

    // Role-based routing on login success
    LaunchedEffect(uiState.user) {
        uiState.user?.let { user ->
            when {
                user.isAdmin -> onNavigateToAdmin()
                user.isVendor -> onNavigateToVendor()
                user.isUser -> onNavigateToUser()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(spacingHeight))

            // Compact Branding Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_app_logo_compact),
                    contentDescription = "VeloGo Logo",
                    tint = accentColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "VeloGo",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(headerSpacing))

            // Premium Transparent Car Hero Asset
            Image(
                painter = painterResource(id = R.drawable.hero_login),
                contentDescription = "Premium Car Hero",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroImageHeight)
                    .padding(vertical = 4.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(headerSpacing))

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = titleFontSize,
                    lineHeight = titleLineHeight
                ),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Sign in to rent your dream ride",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(spacingHeight))

            // Main Input Container Card with depth and borders
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isDark) accentColor.copy(alpha = 0.15f) else accentColor.copy(alpha = 0.12f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(cardPadding)
                ) {
                    // Email Input
                    RentARideTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                            viewModel.clearError()
                        },
                        label = "Email Address",
                        placeholder = "name@example.com",
                        leadingIcon = Icons.Default.Email,
                        errorText = emailError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        accentColor = accentColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Input
                    RentARideTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                            viewModel.clearError()
                        },
                        label = "Password",
                        placeholder = "••••••••",
                        leadingIcon = Icons.Default.Lock,
                        errorText = passwordError,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        accentColor = accentColor
                    )

                    Spacer(modifier = Modifier.height(innerSpacing))

                    // Error Message Display
                    AnimatedVisibility(
                        visible = uiState.error != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = uiState.error ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Login Button
                    RentARideButton(
                        text = "Sign In",
                        onClick = {
                            var isValid = true
                            if (email.isBlank()) {
                                emailError = "email required"
                                isValid = false
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailError = "Invalid email address"
                                isValid = false
                            }

                            if (password.isBlank()) {
                                passwordError = "password required"
                                isValid = false
                            }

                            if (isValid) {
                                viewModel.signIn(email, password)
                            }
                        },
                        isLoading = uiState.isLoading,
                        containerColor = accentColor,
                        contentColor = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // SignUp Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Don't have an account? ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Sign Up",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.clickable { onNavigateToSignUp() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacingHeight))

            // Divider or "OR" text
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                )
                Text(
                    text = "OR CONTINUE WITH",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                )
            }

            Spacer(modifier = Modifier.height(innerSpacing))

            // Google Sign-In
            GoogleSignInButton(
                onClick = {
                    googleSignInClient.signOut().addOnCompleteListener {
                        val signInIntent = googleSignInClient.signInIntent
                        googleLauncher.launch(signInIntent)
                    }
                },
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(bottomSpacing))

            // Switch to Vendor Link
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                ),
                onClick = { onNavigateToVendorSignIn() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Vendor info",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Are you a Vendor? Sign in here",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacingHeight))
        }
    }
}
