package com.example.frontend.ui.screens.auth

import android.widget.Toast
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.frontend.ui.components.VeloGoButton
import com.example.frontend.ui.components.VeloGoTextField
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun VendorSignUpScreen(
    viewModel: AuthViewModel,
    onNavigateToVendorSignIn: () -> Unit,
    onNavigateToVendorDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Royal Blue / Indigo Vendor Accent Color
    val accentColor = Color(0xFF3D5AFE)

    val isDark = isSystemInDarkTheme()

    // Highly subtle and elegant background gradient (Vendor Theme)
    val bgGradient = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(Color(0xFF0A0A0A), Color(0xFF0B0D13))
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(Color(0xFFF6F8FA), Color(0xFFEDF2F7))
            )
        }
    }

    // Fluid Adaptive Sizing Calculations
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val heroImageHeight = (screenHeight * 0.16f).coerceIn(80.dp, 130.dp)
    val spacingHeight = (screenHeight * 0.03f).coerceIn(8.dp, 24.dp)
    val headerSpacing = (screenHeight * 0.02f).coerceIn(6.dp, 12.dp)
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

            viewModel.signInWithGoogle(name, userEmail, photo, isVendor = true)
        } catch (e: Exception) {
            // Handle error
        }
    }

    // Google Sign-In Success Navigation
    LaunchedEffect(uiState.user) {
        uiState.user?.let { user ->
            if (user.isVendor) {
                onNavigateToVendorDashboard()
            }
        }
    }

    // Success Toast on Traditional SignUp
    LaunchedEffect(uiState.signupSuccessMessage) {
        uiState.signupSuccessMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessMessage()
            onNavigateToVendorSignIn()
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
                painter = painterResource(id = R.drawable.promo_car),
                contentDescription = "Premium Car Hero",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroImageHeight)
                    .padding(vertical = 4.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(headerSpacing))

            Text(
                text = "Register as Vendor",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = titleFontSize,
                    lineHeight = titleLineHeight
                ),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Create vendor account to list vehicles",
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
                    // Username Input
                    VeloGoTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            usernameError = null
                            viewModel.clearError()
                        },
                        label = "Username",
                        placeholder = "Choose a vendor profile name",
                        leadingIcon = Icons.Default.Person,
                        errorText = usernameError,
                        accentColor = accentColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Email Input
                    VeloGoTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                            viewModel.clearError()
                        },
                        label = "Vendor Email",
                        placeholder = "vendor@example.com",
                        leadingIcon = Icons.Default.Email,
                        errorText = emailError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        accentColor = accentColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Input
                    VeloGoTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                            viewModel.clearError()
                        },
                        label = "Password",
                        placeholder = "Minimum 4 characters",
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

                    // Register Button
                    VeloGoButton(
                        text = "Register as Vendor",
                        onClick = {
                            var isValid = true
                            if (username.length < 3) {
                                usernameError = "minimum 3 characters required"
                                isValid = false
                            }
                            if (email.isBlank()) {
                                emailError = "email required"
                                isValid = false
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailError = "Invalid email address"
                                isValid = false
                            }
                            if (password.length < 4) {
                                passwordError = "minimum 4 characters required"
                                isValid = false
                            }

                            if (isValid) {
                                viewModel.vendorSignUp(username, email, password)
                            }
                        },
                        isLoading = uiState.isLoading,
                        containerColor = accentColor,
                        contentColor = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // SignIn Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Already have a vendor account? ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.clickable { onNavigateToVendorSignIn() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacingHeight))

            // Divider
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

            Spacer(modifier = Modifier.height(spacingHeight))
        }
    }
}
