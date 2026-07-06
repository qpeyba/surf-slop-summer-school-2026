package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingStatus
import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingWithSlot
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Card
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextSecondary
import com.qpeyba.surf_slop_summer_school_2026.util.DateFormatter

@Composable
fun BookingCard(
    bookingWithSlot: BookingWithSlot,
    onCancelClick: (() -> Unit)? = null,
    onTransferClick: (() -> Unit)? = null,
    onRateClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val booking = bookingWithSlot.booking
    val slot = bookingWithSlot.slot

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            if (slot.photoUrls.isNotEmpty()) {
                AsyncImage(
                    model = slot.photoUrls.first(),
                    contentDescription = slot.menu ?: "Фото",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = slot.menu ?: "Программа",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    StatusBadge(status = booking.status)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = DateFormatter.toReadableDateTime(slot.dateTime),
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                if (slot.instructor != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    ChefInfoRow(
                        name = slot.instructor.name,
                        rating = slot.instructor.rating
                    )
                }

                if (booking.status == BookingStatus.COMPLETED) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (booking.reviewRating != null) {
                        StarRatingDisplay(rating = booking.reviewRating.toDouble())
                        if (onRateClick != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            ChefSecondaryButton(text = "Изменить оценку", onClick = onRateClick)
                        }
                    } else if (onRateClick != null) {
                        ChefButton(text = "Оценить шефа", onClick = onRateClick)
                    }
                }

                if (booking.status == BookingStatus.CANCELLED_BY_CLIENT || booking.status == BookingStatus.CANCELLED_BY_STUDIO) {
                    if (booking.refundAmount != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Возврат: ${com.qpeyba.surf_slop_summer_school_2026.util.PriceFormatter.format(booking.refundAmount)}",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (booking.status == BookingStatus.ACTIVE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onTransferClick != null) {
                            ChefSecondaryButton(text = "Перенести", onClick = onTransferClick, modifier = Modifier.weight(1f))
                        }
                        if (onCancelClick != null) {
                            ChefDestructiveButton(text = "Отменить", onClick = onCancelClick, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
