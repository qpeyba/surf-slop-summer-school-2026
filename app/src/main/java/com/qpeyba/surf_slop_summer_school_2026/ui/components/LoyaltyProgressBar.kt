package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Olive
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextSecondary

@Composable
fun LoyaltyProgressBar(
    points: Int,
    status: String?,
    nextLevelPoints: Int? = null,
    modifier: Modifier = Modifier
) {
    val progress = if (nextLevelPoints != null && nextLevelPoints > 0) {
        points.toFloat() / nextLevelPoints
    } else {
        1f
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = if (status != null) "$points баллов · $status" else "$points баллов",
            fontSize = 13.sp,
            color = TextSecondary
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = Olive,
            trackColor = Olive.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round
        )
        if (nextLevelPoints != null) {
            Text(
                text = "До следующего уровня: ${nextLevelPoints - points} баллов",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}
