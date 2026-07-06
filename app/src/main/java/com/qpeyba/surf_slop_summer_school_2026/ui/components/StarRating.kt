package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Terracotta
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextSecondary

@Composable
fun StarRating(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxStars: Int = 5
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxStars) { index ->
            IconButton(
                onClick = { onRatingChanged(index + 1) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "${index + 1} из $maxStars",
                    tint = if (index < rating) Terracotta else TextSecondary
                )
            }
        }
        Text(
            text = "$rating из $maxStars",
            color = TextSecondary,
            modifier = Modifier
        )
    }
}
