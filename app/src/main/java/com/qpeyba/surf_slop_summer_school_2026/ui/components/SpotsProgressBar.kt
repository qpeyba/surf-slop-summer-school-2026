package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.SlotAvailable
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.SlotFull
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextSecondary
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Terracotta

@Composable
fun SpotsProgressBar(
    bookedCount: Int,
    capacity: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (capacity > 0) bookedCount.toFloat() / capacity else 0f
    val spotsLeft = capacity - bookedCount
    val barColor = when {
        spotsLeft <= 0 -> SlotFull
        spotsLeft <= 3 -> Terracotta
        else -> SlotAvailable
    }

    Column(modifier = modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = barColor,
            trackColor = barColor.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$bookedCount из $capacity мест",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}
