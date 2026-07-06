package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qpeyba.surf_slop_summer_school_2026.domain.model.Difficulty
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Olive
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.OliveLight
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.WarningOrange

@Composable
fun DifficultyBadge(
    difficulty: Difficulty,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (difficulty) {
        Difficulty.BEGINNER -> OliveLight to Olive
        Difficulty.EXPERIENCED -> WarningOrange.copy(alpha = 0.15f) to WarningOrange
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = difficulty.apiValue,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
