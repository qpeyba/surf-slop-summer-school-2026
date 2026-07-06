package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Terracotta
import com.qpeyba.surf_slop_summer_school_2026.util.PriceFormatter

@Composable
fun PriceTag(
    amount: Long,
    modifier: Modifier = Modifier
) {
    Text(
        text = PriceFormatter.format(amount),
        color = Terracotta,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = modifier
    )
}
