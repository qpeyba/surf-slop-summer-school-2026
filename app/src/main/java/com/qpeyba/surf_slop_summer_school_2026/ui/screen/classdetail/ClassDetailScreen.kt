package com.qpeyba.surf_slop_summer_school_2026.ui.screen.classdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qpeyba.surf_slop_summer_school_2026.domain.model.Slot
import com.qpeyba.surf_slop_summer_school_2026.ui.components.AddressRow
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefButton
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefInfoRow
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefTopAppBar
import com.qpeyba.surf_slop_summer_school_2026.ui.components.DifficultyBadge
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ErrorState
import com.qpeyba.surf_slop_summer_school_2026.ui.components.LoadingSkeleton
import com.qpeyba.surf_slop_summer_school_2026.ui.components.PhotoCarousel
import com.qpeyba.surf_slop_summer_school_2026.ui.components.PriceTag
import com.qpeyba.surf_slop_summer_school_2026.ui.components.SkeletonVariant
import com.qpeyba.surf_slop_summer_school_2026.ui.components.SpotsProgressBar
import com.qpeyba.surf_slop_summer_school_2026.ui.components.StarRatingDisplay
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextSecondary
import com.qpeyba.surf_slop_summer_school_2026.util.DateFormatter
import com.qpeyba.surf_slop_summer_school_2026.util.UiState

@Composable
fun ClassDetailScreen(
    onBook: (String) -> Unit,
    viewModel: ClassDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        ChefTopAppBar(title = "Детали класса", onBackClick = {})

        when (val slotState = state.slot) {
            is UiState.Loading -> {
                LoadingSkeleton(variant = SkeletonVariant.DETAIL)
            }
            is UiState.Error -> {
                ErrorState(
                    message = slotState.message,
                    onRetry = { /* reload */ }
                )
            }
            is UiState.Empty -> { }
            is UiState.Success -> {
                val slot = slotState.data
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    PhotoCarousel(
                        photoUrls = slot.photoUrls,
                        height = 200.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = slot.menu ?: "Программа",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DifficultyBadge(difficulty = slot.difficulty)
                            Text(
                                text = "3 часа",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }

                        if (slot.menu != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = slot.menu, fontSize = 14.sp, color = TextSecondary)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Шеф",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (slot.instructor != null) {
                            ChefInfoRow(
                                name = slot.instructor.name,
                                rating = slot.instructor.rating
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = slot.instructor.specialization,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        AddressRow(address = slot.address)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = DateFormatter.toReadableDateTime(slot.dateTime),
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        SpotsProgressBar(
                            bookedCount = slot.bookedCount,
                            capacity = slot.capacity,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        PriceTag(amount = slot.price)
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    ChefButton(
                        text = state.ctaText,
                        onClick = { if (state.isBookingAvailable) onBook(slot.id) },
                        enabled = state.isBookingAvailable
                    )
                }
            }
        }
    }
}
