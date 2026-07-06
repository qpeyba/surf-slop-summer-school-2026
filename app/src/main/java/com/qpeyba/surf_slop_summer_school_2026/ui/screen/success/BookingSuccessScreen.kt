package com.qpeyba.surf_slop_summer_school_2026.ui.screen.success

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qpeyba.surf_slop_summer_school_2026.ui.components.AddressRow
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefButton
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefInfoRow
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ErrorState
import com.qpeyba.surf_slop_summer_school_2026.ui.components.LoadingSkeleton
import com.qpeyba.surf_slop_summer_school_2026.ui.components.PriceTag
import com.qpeyba.surf_slop_summer_school_2026.ui.components.SkeletonVariant
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Card
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.SuccessGreen
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextSecondary
import com.qpeyba.surf_slop_summer_school_2026.util.DateFormatter
import com.qpeyba.surf_slop_summer_school_2026.util.UiState

@Composable
fun BookingSuccessScreen(
    onNavigateToMyBookings: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    viewModel: BookingSuccessViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    when (val bookingState = state.booking) {
        is UiState.Loading -> {
            LoadingSkeleton(variant = SkeletonVariant.DETAIL)
        }
        is UiState.Error -> {
            ErrorState(message = bookingState.message)
        }
        is UiState.Empty -> { }
        is UiState.Success -> {
            val booking = bookingState.data.booking
            val slot = bookingState.data.slot

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = SuccessGreen
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Вы записаны!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFF2F2F2))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = slot.menu ?: "Программа",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = DateFormatter.toReadableDateTime(slot.dateTime),
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        if (slot.instructor != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            ChefInfoRow(name = slot.instructor.name, rating = slot.instructor.rating)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        AddressRow(address = slot.address)
                        Spacer(modifier = Modifier.height(8.dp))
                        PriceTag(amount = slot.price)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Напоминания придут за 24ч, 3ч и 30 минут до начала",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))
                ChefButton(text = "Мои брони", onClick = onNavigateToMyBookings)
                Spacer(modifier = Modifier.height(8.dp))
                com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefSecondaryButton(
                    text = "В расписание",
                    onClick = onNavigateToSchedule
                )
            }
        }
    }
}
