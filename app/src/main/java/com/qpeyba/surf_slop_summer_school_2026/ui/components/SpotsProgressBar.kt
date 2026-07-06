package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.SlotFull
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.SlotAvailable
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Terracotta
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Card

@Composable
fun SpotsProgressBar(
    bookedCount: Int,
    capacity: Int,
    modifier: Modifier = Modifier,
    alpha: Float = 1f
) {
    val freeCount = capacity - bookedCount
    val barColor = when {
        freeCount <= 0 -> SlotFull
        freeCount <= 3 -> Terracotta
        else -> SlotAvailable
    }

    Surface(
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(16.dp),
        color = Card
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Свободно мест",
                fontSize = 14.sp,
                color = TextPrimary.copy(alpha = alpha)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (freeCount <= 0) {
                    Text(
                        text = "Мест нет",
                        fontSize = 14.sp,
                        color = SlotFull.copy(alpha = alpha)
                    )
                } else {
                    Text(
                        text = "$freeCount из $capacity",
                        fontSize = 14.sp,
                        color = TextPrimary.copy(alpha = alpha)
                    )
                }
            }
        }
    }
}
