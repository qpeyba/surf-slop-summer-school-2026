package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.qpeyba.surf_slop_summer_school_2026.domain.model.Slot
import com.qpeyba.surf_slop_summer_school_2026.domain.model.SlotStatus
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary
import com.qpeyba.surf_slop_summer_school_2026.util.DateFormatter
import com.qpeyba.surf_slop_summer_school_2026.util.PriceFormatter

@Composable
fun ClassCard(
    slot: Slot,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha = if (slot.status == SlotStatus.CANCELLED_BY_STUDIO) 0.5f else 1f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = slot.status != SlotStatus.CANCELLED_BY_STUDIO) { onCardClick(slot.id) },
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFFF2F2F2)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (slot.photoUrls.isNotEmpty()) {
                AsyncImage(
                    model = slot.photoUrls.first(),
                    contentDescription = slot.menu ?: "Фото класса",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                    alpha = alpha
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DifficultyBadge(difficulty = slot.difficulty)
                }
                Text(
                    text = DateFormatter.toReadableDateTime(slot.dateTime),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = TextPrimary.copy(alpha = alpha)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (slot.instructor != null) "Шеф: ${slot.instructor.name}" else "",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary.copy(alpha = alpha)
                )
                Text(
                    text = PriceFormatter.format(slot.price),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary.copy(alpha = alpha)
                )
            }

            SpotsProgressBar(
                bookedCount = slot.bookedCount,
                capacity = slot.capacity,
                modifier = Modifier.fillMaxWidth(),
                alpha = alpha
            )
        }
    }
}
