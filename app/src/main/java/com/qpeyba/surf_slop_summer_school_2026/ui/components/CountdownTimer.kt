package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun CountdownTimer(
    seconds: Int,
    modifier: Modifier = Modifier
) {
    var remaining by remember { mutableIntStateOf(seconds) }

    LaunchedEffect(seconds) {
        remaining = seconds
        while (remaining > 0) {
            delay(1000L)
            remaining--
        }
    }

    val minutes = remaining / 60
    val secs = remaining % 60
    Text(
        text = "Отправить код повторно через $minutes:${secs.toString().padStart(2, '0')}",
        color = TextSecondary,
        modifier = modifier
    )
}
