package com.qpeyba.surf_slop_summer_school_2026.ui.screen.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefButton
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefChip
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefInfoRow
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefTopAppBar
import com.qpeyba.surf_slop_summer_school_2026.ui.components.EquipmentSelector
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ErrorState
import com.qpeyba.surf_slop_summer_school_2026.ui.components.LoadingSkeleton
import com.qpeyba.surf_slop_summer_school_2026.ui.components.PriceTag
import com.qpeyba.surf_slop_summer_school_2026.ui.components.SkeletonVariant
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.DestructiveRed
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextSecondary
import com.qpeyba.surf_slop_summer_school_2026.util.DateFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookingFormScreen(
    onSuccess: (String) -> Unit,
    viewModel: BookingFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BookingFormEffect.ShowSuccess -> onSuccess(effect.bookingId)
                is BookingFormEffect.ShowError -> { }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ChefTopAppBar(title = "Бронирование", onBackClick = {})

        if (state.isLoading) {
            LoadingSkeleton(variant = SkeletonVariant.DETAIL)
        } else if (state.error != null && state.slot == null) {
            ErrorState(message = state.error!!)
        } else {
            val slot = state.slot ?: return

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = slot.menu ?: "Программа",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = DateFormatter.toReadableDateTime(slot.dateTime),
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                if (slot.instructor != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ChefInfoRow(name = slot.instructor.name, rating = slot.instructor.rating)
                }
                Spacer(modifier = Modifier.height(4.dp))
                PriceTag(amount = slot.price)

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Экипировка",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                EquipmentSelector(
                    selected = state.equipment,
                    onSelectionChanged = { viewModel.onEvent(BookingFormEvent.SelectEquipment(it)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Аллергии",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                val allergies = state.profile?.allergies ?: emptyList()
                if (allergies.isEmpty()) {
                    Text(text = "Не указаны", fontSize = 14.sp, color = TextSecondary)
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allergies.forEach { allergy ->
                            ChefChip(text = allergy, onRemove = null)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Итого",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                PriceTag(amount = state.totalPrice)

                val formError = state.error
                if (formError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formError,
                        fontSize = 13.sp,
                        color = DestructiveRed
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                ChefButton(
                    text = "Подтвердить бронирование",
                    onClick = { viewModel.onEvent(BookingFormEvent.ConfirmPressed) },
                    isLoading = state.isSubmitting,
                    enabled = !state.isSubmitting
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Скоро: онлайн-оплата",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
