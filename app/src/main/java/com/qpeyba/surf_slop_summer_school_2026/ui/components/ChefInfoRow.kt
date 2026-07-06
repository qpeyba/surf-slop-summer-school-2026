package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextSecondary

@Composable
fun ChefInfoRow(
    name: String,
    rating: Double? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier,
            tint = TextSecondary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = name,
            fontSize = 14.sp,
            color = TextPrimary
        )
        if (rating != null) {
            Spacer(modifier = Modifier.width(8.dp))
            StarRatingDisplay(rating = rating)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = rating.toString(),
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}
