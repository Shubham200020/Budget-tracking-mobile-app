package com.example.skmaccount.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.NumberFormat
import java.util.Locale

object CategoryHelper {
    fun getIcon(name: String): ImageVector {
        return when (name.lowercase()) {
            "food" -> Icons.Default.Restaurant
            "transport" -> Icons.Default.DirectionsCar
            "shopping" -> Icons.Default.ShoppingCart
            "utilities" -> Icons.Default.Build
            "entertainment" -> Icons.Default.Movie
            "health" -> Icons.Default.MedicalServices
            "salary" -> Icons.Default.MonetizationOn
            else -> Icons.Default.Category
        }
    }

    fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
        formatter.maximumFractionDigits = 0
        return "₹" + formatter.format(amount)
    }
}
