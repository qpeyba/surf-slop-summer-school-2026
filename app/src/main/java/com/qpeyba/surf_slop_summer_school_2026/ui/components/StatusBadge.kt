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
import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingStatus
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.CancelledGrey
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.DestructiveRed
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.InfoBlue
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.SuccessGreen

@Composable
fun StatusBadge(
    status: BookingStatus,
    modifier: Modifier = Modifier
) {
    val bgColor: Color
    val textColor: Color
    val label: String

    when (status) {
        BookingStatus.ACTIVE -> {
            bgColor = InfoBlue.copy(alpha = 0.1f)
            textColor = InfoBlue
            label = "Активна"
        }
        BookingStatus.CANCELLED_BY_CLIENT -> {
            bgColor = CancelledGrey.copy(alpha = 0.2f)
            textColor = CancelledGrey
            label = "Отменена"
        }
        BookingStatus.CANCELLED_BY_STUDIO -> {
            bgColor = CancelledGrey.copy(alpha = 0.2f)
            textColor = CancelledGrey
            label = "Отменена студией"
        }
        BookingStatus.COMPLETED -> {
            bgColor = SuccessGreen.copy(alpha = 0.1f)
            textColor = SuccessGreen
            label = "Завершена"
        }
        BookingStatus.CLIENT_NO_SHOW -> {
            bgColor = DestructiveRed.copy(alpha = 0.1f)
            textColor = DestructiveRed
            label = "Не пришёл"
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
