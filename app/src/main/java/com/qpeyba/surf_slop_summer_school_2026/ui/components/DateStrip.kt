package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Card
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Terracotta
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextSecondary
import com.qpeyba.surf_slop_summer_school_2026.util.DateFormatter
import java.time.LocalDate

data class DateItem(
    val date: LocalDate,
    val isToday: Boolean,
    val isSelected: Boolean
)

@Composable
fun DateStrip(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dates) { date ->
            val isSelected = date == selectedDate
            val isToday = date == LocalDate.now()
            DateCell(
                date = date,
                isSelected = isSelected,
                isToday = isToday,
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@Composable
private fun DateCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) Terracotta else Card
    val textColor = if (isSelected) Card else if (isToday) Terracotta else TextPrimary
    val subTextColor = if (isSelected) Card.copy(alpha = 0.8f) else TextSecondary

    Box(
        modifier = Modifier
            .width(64.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = DateFormatter.toDayOfWeekShort(date),
                fontSize = 11.sp,
                color = subTextColor
            )
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = date.month.name.take(3),
                fontSize = 10.sp,
                color = subTextColor
            )
        }
    }
}
