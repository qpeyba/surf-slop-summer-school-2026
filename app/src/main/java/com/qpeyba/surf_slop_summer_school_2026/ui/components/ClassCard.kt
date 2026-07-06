package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.qpeyba.surf_slop_summer_school_2026.domain.model.Slot
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Card
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextSecondary
import com.qpeyba.surf_slop_summer_school_2026.util.DateFormatter
import com.qpeyba.surf_slop_summer_school_2026.util.PriceFormatter

@Composable
fun ClassCard(
    slot: Slot,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable { onCardClick(slot.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            if (slot.photoUrls.isNotEmpty()) {
                AsyncImage(
                    model = slot.photoUrls.first(),
                    contentDescription = slot.menu ?: "Фото класса",
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = slot.menu ?: "Программа",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    DifficultyBadge(difficulty = slot.difficulty)
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (slot.instructor != null) {
                    ChefInfoRow(
                        name = slot.instructor.name,
                        rating = slot.instructor.rating,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                AddressRow(address = slot.address)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = DateFormatter.toReadableDateTime(slot.dateTime),
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    PriceTag(amount = slot.price)
                }
                Spacer(modifier = Modifier.height(8.dp))
                SpotsProgressBar(
                    bookedCount = slot.bookedCount,
                    capacity = slot.capacity,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
