package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Olive
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.OliveLight
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary

@Composable
fun ChefChip(
    text: String,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = OliveLight
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, end = if (onRemove != null) 4.dp else 12.dp, top = 6.dp, bottom = 6.dp)
        ) {
            Text(text = text, color = TextPrimary, fontSize = 13.sp)
            if (onRemove != null) {
                IconButton(onClick = onRemove, modifier = Modifier) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Удалить $text",
                        tint = TextPrimary
                    )
                }
            }
        }
    }
}
