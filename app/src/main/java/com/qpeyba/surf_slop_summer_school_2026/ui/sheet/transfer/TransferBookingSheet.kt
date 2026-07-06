package com.qpeyba.surf_slop_summer_school_2026.ui.sheet.transfer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingWithSlot
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefButton
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefInfoRow
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefTopAppBar
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ClassCard
import com.qpeyba.surf_slop_summer_school_2026.ui.components.EmptyState
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ErrorState
import com.qpeyba.surf_slop_summer_school_2026.ui.components.LoadingSkeleton
import com.qpeyba.surf_slop_summer_school_2026.ui.components.SkeletonVariant
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextSecondary
import com.qpeyba.surf_slop_summer_school_2026.util.DateFormatter
import com.qpeyba.surf_slop_summer_school_2026.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferBookingSheet(
    bookingWithSlot: BookingWithSlot,
    onDismiss: () -> Unit,
    onConfirmed: () -> Unit,
    viewModel: TransferBookingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(bookingWithSlot) {
        viewModel.init(bookingWithSlot)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        when (state.step) {
            TransferStep.SELECT -> {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Перенос брони",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Текущая бронь:", fontSize = 14.sp, color = TextSecondary)
                    Text(
                        text = bookingWithSlot.slot.menu ?: "Программа",
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = DateFormatter.toReadableDateTime(bookingWithSlot.slot.dateTime),
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    if (bookingWithSlot.slot.instructor != null) {
                        ChefInfoRow(name = bookingWithSlot.slot.instructor.name)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Выберите новый слот:",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    when (val slotsState = state.availableSlots) {
                        is UiState.Loading -> {
                            LoadingSkeleton(variant = SkeletonVariant.LIST)
                        }
                        is UiState.Empty -> {
                            EmptyState(
                                illustration = Icons.Default.SearchOff,
                                title = "Нет доступных слотов"
                            )
                        }
                        is UiState.Error -> {
                            ErrorState(message = slotsState.message)
                        }
                        is UiState.Success -> {
                            LazyColumn(
                                contentPadding = PaddingValues(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(slotsState.data) { slot ->
                                    ClassCard(
                                        slot = slot,
                                        onCardClick = { viewModel.onEvent(TransferBookingEvent.SlotSelected(slot)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            TransferStep.CONFIRM -> {
                val oldSlot = state.oldBooking.slot
                val newSlot = state.selectedSlot ?: return@ModalBottomSheet

                Column(modifier = Modifier.padding(16.dp)) {
                    IconButton(onClick = { viewModel.onEvent(TransferBookingEvent.BackPressed) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                    Text(
                        text = "Подтверждение переноса",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Было:", fontWeight = FontWeight.Medium, color = TextSecondary)
                    Text(oldSlot.menu ?: "Программа", color = TextPrimary)
                    Text(DateFormatter.toReadableDateTime(oldSlot.dateTime), fontSize = 13.sp, color = TextSecondary)
                    if (oldSlot.instructor != null) ChefInfoRow(name = oldSlot.instructor.name)

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Будет:", fontWeight = FontWeight.Medium, color = TextSecondary)
                    Text(newSlot.menu ?: "Программа", color = TextPrimary)
                    Text(DateFormatter.toReadableDateTime(newSlot.dateTime), fontSize = 13.sp, color = TextSecondary)
                    if (newSlot.instructor != null) ChefInfoRow(name = newSlot.instructor.name)

                    Spacer(modifier = Modifier.height(24.dp))
                    ChefButton(
                        text = "Перенести",
                        onClick = {
                            viewModel.onEvent(TransferBookingEvent.ConfirmPressed)
                            onConfirmed()
                        },
                        isLoading = state.isProcessing
                    )
                }
            }
        }
    }
}
