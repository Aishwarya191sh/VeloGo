package com.example.frontend.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.frontend.ui.theme.SlateGrey
import java.util.*

// 1. Shimmer loading modifier
fun Modifier.shimmerLoading(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    background(brush = brush)
}

// 1a. Bounce click scale modifier
fun Modifier.bounceClick(interactionSource: MutableInteractionSource): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce_click_scale"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

// 2. Rating Bar
@Composable
fun RatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    stars: Int = 5,
    starsColor: Color = Color(0xFFFFD700)
) {
    Row(modifier = modifier) {
        repeat(stars) { index ->
            Icon(
                imageVector = if (index < rating.toInt()) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Star $index",
                tint = if (index < rating.toInt()) starsColor else SlateGrey,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// 3. Image Carousel using HorizontalPager
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageCarousel(
    images: List<String>,
    modifier: Modifier = Modifier
) {
    if (images.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("No Image Available")
        }
    } else {
        val pagerState = rememberPagerState(pageCount = { images.size })
        Box(modifier = modifier.fillMaxWidth()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(images[page])
                        .crossfade(true)
                        .build(),
                    contentDescription = "Carousel image $page",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Page Indicator Dots
            Row(
                Modifier
                    .height(30.dp)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(images.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color.White else Color.LightGray.copy(alpha = 0.5f)
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}

// 4. Custom Button with optional loading state
@Composable
fun VeloGoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_press_scale"
    )

    val buttonBrush = if (enabled && !isLoading) {
        Brush.horizontalGradient(
            colors = listOf(
                containerColor,
                Color(
                    red = (containerColor.red * 0.85f).coerceAtLeast(0f),
                    green = (containerColor.green * 0.85f).coerceAtLeast(0f),
                    blue = (containerColor.blue * 0.85f).coerceAtLeast(0f)
                )
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(containerColor.copy(alpha = 0.5f), containerColor.copy(alpha = 0.5f))
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .background(buttonBrush),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = contentColor,
            disabledContainerColor = Color.Transparent
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 1.dp,
            focusedElevation = 4.dp
        ),
        contentPadding = PaddingValues(0.dp),
        interactionSource = interactionSource
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = contentColor,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
        }
    }
}

// 5. Custom Text Field
@Composable
fun VeloGoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    errorText: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val textElevation by animateFloatAsState(
        targetValue = if (isFocused) 3f else 0f,
        animationSpec = tween(150),
        label = "text_field_elevation"
    )

    val containerBgColor by animateColorAsState(
        targetValue = if (isSystemInDarkTheme()) {
            if (isFocused) accentColor.copy(alpha = 0.05f) else Color(0xFF161616)
        } else {
            if (isFocused) accentColor.copy(alpha = 0.03f) else Color(0xFFF5F5F5)
        },
        animationSpec = tween(150),
        label = "textfield_bg"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = leadingIcon?.let {
                { Icon(imageVector = it, contentDescription = null) }
            },
            trailingIcon = trailingIcon,
            isError = errorText != null,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
                .graphicsLayer {
                    shadowElevation = textElevation
                    shape = RoundedCornerShape(18.dp)
                    clip = true
                },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = SlateGrey.copy(alpha = 0.22f),
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = accentColor,
                unfocusedLabelColor = SlateGrey,
                focusedLeadingIconColor = accentColor,
                unfocusedLeadingIconColor = SlateGrey,
                cursorColor = accentColor,
                focusedContainerColor = containerBgColor,
                unfocusedContainerColor = containerBgColor,
                errorContainerColor = containerBgColor
            )
        )
        if (errorText != null) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}

// 5a. Google Sign-In Button
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Continue with Google",
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "google_button_press"
    )

    val isDark = isSystemInDarkTheme()
    val containerColor = if (isDark) Color(0xFF131314) else Color.White
    val contentColor = if (isDark) Color(0xFFE3E3E3) else Color(0xFF1F1F1F)
    val borderColor = if (isDark) Color(0xFF8E918F) else Color(0xFF747775)

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = com.example.frontend.R.drawable.ic_google_logo),
                contentDescription = "Google Logo",
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            )
        }
    }
}

// 5b. Premium Loading Indicator
@Composable
fun PremiumLoadingIndicator(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "loading_anim")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading_rotation"
    )
    val scale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading_scale"
    )

    Box(
        modifier = modifier
            .size(54.dp)
            .graphicsLayer {
                rotationZ = rotation
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = accentColor,
            trackColor = accentColor.copy(alpha = 0.1f),
            strokeWidth = 3.5.dp,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// 6. Generic Empty / Error state view
@Composable
fun EmptyOrErrorStateView(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "No Data Found",
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = SlateGrey,
            textAlign = TextAlign.Center
        )
        if (actionButtonText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onActionClick) {
                Text(actionButtonText)
            }
        }
    }
}

// 7. Date Picker Dialog Picker helper
@Composable
fun VeloGoDatePicker(
    label: String,
    selectedDate: Calendar?,
    onDateSelected: (Calendar) -> Unit,
    modifier: Modifier = Modifier,
    minDate: Calendar = Calendar.getInstance()
) {
    val context = LocalContext.current
    val year = selectedDate?.get(Calendar.YEAR) ?: minDate.get(Calendar.YEAR)
    val month = selectedDate?.get(Calendar.MONTH) ?: minDate.get(Calendar.MONTH)
    val day = selectedDate?.get(Calendar.DAY_OF_MONTH) ?: minDate.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth)
                set(Calendar.DAY_OF_MONTH, selectedDay)
            }
            onDateSelected(calendar)
        },
        year, month, day
    ).apply {
        datePicker.minDate = minDate.timeInMillis
    }

    val displayString = selectedDate?.let {
        "${it.get(Calendar.DAY_OF_MONTH)}/${it.get(Calendar.MONTH) + 1}/${it.get(Calendar.YEAR)}"
    } ?: "Select Date"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { datePickerDialog.show() }
    ) {
        OutlinedTextField(
            value = displayString,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onBackground,
                disabledLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                disabledBorderColor = SlateGrey.copy(alpha = 0.5f)
            )
        )
    }
}

// 8. Time Picker Dialog helper
@Composable
fun VeloGoTimePicker(
    label: String,
    selectedTime: Calendar?,
    onTimeSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentCalendar = Calendar.getInstance()
    val hour = selectedTime?.get(Calendar.HOUR_OF_DAY) ?: currentCalendar.get(Calendar.HOUR_OF_DAY)
    val minute = selectedTime?.get(Calendar.MINUTE) ?: currentCalendar.get(Calendar.MINUTE)

    val timePickerDialog = TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            onTimeSelected(selectedHour, selectedMinute)
        },
        hour, minute, true
    )

    val displayString = selectedTime?.let {
        String.format(Locale.getDefault(), "%02d:%02d", it.get(Calendar.HOUR_OF_DAY), it.get(Calendar.MINUTE))
    } ?: "Select Time"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { timePickerDialog.show() }
    ) {
        OutlinedTextField(
            value = displayString,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onBackground,
                disabledLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                disabledBorderColor = SlateGrey.copy(alpha = 0.5f)
            )
        )
    }
}
