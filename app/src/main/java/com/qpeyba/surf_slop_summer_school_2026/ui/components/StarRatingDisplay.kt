package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Terracotta
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextSecondary

@Composable
fun StarRatingDisplay(
    rating: Double,
    modifier: Modifier = Modifier,
    maxStars: Int = 5
) {
    Row(modifier = modifier) {
        repeat(maxStars) { index ->
            val icon = when {
                index < rating.toInt() -> Icons.Default.Star
                index < rating && rating - index >= 0.5 -> Icons.Default.StarHalf
                else -> Icons.Default.StarBorder
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (index < rating || (index < rating + 0.5)) Terracotta else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
