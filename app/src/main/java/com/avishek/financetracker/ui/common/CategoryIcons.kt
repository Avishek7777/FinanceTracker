package com.avishek.financetracker.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Icons are stored by key in the database so they survive app updates. */
val CategoryIcons: Map<String, ImageVector> = linkedMapOf(
    "restaurant" to Icons.Filled.Restaurant,
    "grocery" to Icons.Filled.LocalGroceryStore,
    "transport" to Icons.Filled.DirectionsBus,
    "shopping" to Icons.Filled.ShoppingBag,
    "bills" to Icons.Filled.Receipt,
    "health" to Icons.Filled.LocalHospital,
    "movie" to Icons.Filled.Movie,
    "home" to Icons.Filled.Home,
    "cafe" to Icons.Filled.LocalCafe,
    "clothes" to Icons.Filled.Checkroom,
    "education" to Icons.Filled.School,
    "fitness" to Icons.Filled.FitnessCenter,
    "travel" to Icons.Filled.Flight,
    "phone" to Icons.Filled.PhoneAndroid,
    "pets" to Icons.Filled.Pets,
    "salary" to Icons.Filled.Payments,
    "work" to Icons.Filled.Work,
    "gift" to Icons.Filled.CardGiftcard,
    "investment" to Icons.Filled.ShowChart,
    "savings" to Icons.Filled.Savings,
    "other" to Icons.Filled.Category,
)

val CategoryColors: List<Long> = listOf(
    0xFFE57373, 0xFFF06292, 0xFFBA68C8, 0xFF9575CD, 0xFF7986CB, 0xFF64B5F6,
    0xFF4DB6AC, 0xFF81C784, 0xFFAED581, 0xFFFFD54F, 0xFFFFB74D, 0xFF90A4AE,
)

fun iconFor(key: String): ImageVector = CategoryIcons[key] ?: Icons.Filled.Category

@Composable
fun CategoryBadge(iconKey: String, color: Long, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    Box(
        modifier = modifier
            .size(size)
            .background(Color(color), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(iconFor(iconKey), contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.55f))
    }
}
