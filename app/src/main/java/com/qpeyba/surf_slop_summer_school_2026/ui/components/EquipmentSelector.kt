package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qpeyba.surf_slop_summer_school_2026.domain.model.EquipmentType
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Terracotta
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Card

@Composable
fun EquipmentSelector(
    selected: EquipmentType,
    onSelectionChanged: (EquipmentType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EquipmentType.entries.forEach { type ->
            val isSelected = type == selected
            Surface(
                modifier = Modifier
                    .width(100.dp)
                    .height(44.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) Terracotta else Color.White,
                onClick = { onSelectionChanged(type) }
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (type) {
                            EquipmentType.OWN -> "Своя"
                            EquipmentType.RENTAL -> "Прокатная"
                        },
                        color = if (isSelected) Color.White else TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}
