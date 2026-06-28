package com.example.frontend.ui.screens.search

import com.example.frontend.ui.components.bounceClick
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.frontend.R
import com.example.frontend.ui.components.RentARideTextField
import com.example.frontend.ui.theme.EmeraldPrimary
import com.example.frontend.ui.theme.SlateGrey

@Composable
fun SearchScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RentARideTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search by model or brand",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = { /* Open filters */ },
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface),
                colors = IconButtonDefaults.iconButtonColors(contentColor = EmeraldPrimary)
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Filters")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Browse Categories",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                CategorySearchCard(
                    title = "SUVs & Crossovers",
                    description = "Comfortable, spacious 5-7 seaters for long getaways.",
                    imageRes = R.drawable.b_c,
                    countText = "12 available",
                    onClick = { navController.navigate("search_results") }
                )
            }
            item {
                CategorySearchCard(
                    title = "Sedans",
                    description = "Fuel efficient, comfortable daily commuters.",
                    imageRes = R.drawable.a_b,
                    countText = "18 available",
                    onClick = { navController.navigate("search_results") }
                )
            }
            item {
                CategorySearchCard(
                    title = "Super / Luxury Cars",
                    description = "Turn heads with our elite models and performance sports vehicles.",
                    imageRes = R.drawable.promo_car,
                    countText = "8 available",
                    onClick = { navController.navigate("search_results") }
                )
            }
            item {
                CategorySearchCard(
                    title = "Electric Vehicles",
                    description = "Eco-friendly, futuristic, ultra-smooth electric rides.",
                    imageRes = R.drawable.a_b, // Tesla image
                    countText = "6 available",
                    onClick = { navController.navigate("search_results") }
                )
            }
        }
    }
}

@Composable
fun CategorySearchCard(
    title: String,
    description: String,
    imageRes: Int,
    countText: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .bounceClick(interactionSource),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateGrey,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = countText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldPrimary
                )
            }
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(100.dp)
                    .weight(0.7f)
            )
        }
    }
}
