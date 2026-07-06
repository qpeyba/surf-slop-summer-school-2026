package com.qpeyba.surf_slop_summer_school_2026.ui.screen.mybookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingStatus
import com.qpeyba.surf_slop_summer_school_2026.ui.components.BookingCard
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefTopAppBar
import com.qpeyba.surf_slop_summer_school_2026.ui.components.EmptyState
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ErrorState
import com.qpeyba.surf_slop_summer_school_2026.ui.components.LoadingSkeleton
import com.qpeyba.surf_slop_summer_school_2026.ui.components.SkeletonVariant
import com.qpeyba.surf_slop_summer_school_2026.ui.sheet.cancel.CancelBookingSheet
import com.qpeyba.surf_slop_summer_school_2026.ui.sheet.rate.RateChefSheet
import com.qpeyba.surf_slop_summer_school_2026.ui.sheet.transfer.TransferBookingSheet
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary
import com.qpeyba.surf_slop_summer_school_2026.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    viewModel: MyBookingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        ChefTopAppBar(title = "Мои брони")

        when (val bookingsState = state.bookings) {
            is UiState.Loading -> {
                LoadingSkeleton(variant = SkeletonVariant.LIST)
            }
            is UiState.Empty -> {
                EmptyState(
                    illustration = Icons.Default.EventBusy,
                    title = "У вас пока нет броней",
                    actionLabel = "В расписание",
                    onAction = { /* navigate to schedule */ }
                )
            }
            is UiState.Error -> {
                ErrorState(
                    message = bookingsState.message,
                    onRetry = { viewModel.onEvent(MyBookingsEvent.Refresh) }
                )
            }
            is UiState.Success -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        state.sections.forEach { (status, items) ->
                            val label = when (status) {
                                BookingStatus.ACTIVE -> "Активные"
                                BookingStatus.COMPLETED -> "Завершённые"
                                else -> "Отменённые"
                            }
                            item {
                                Text(
                                    text = "$label (${items.size})",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                )
                            }
                            items(items) { bookingWithSlot ->
                                BookingCard(
                                    bookingWithSlot = bookingWithSlot,
                                    onCancelClick = { viewModel.onEvent(MyBookingsEvent.CancelPressed(bookingWithSlot)) },
                                    onTransferClick = { viewModel.onEvent(MyBookingsEvent.TransferPressed(bookingWithSlot)) },
                                    onRateClick = { viewModel.onEvent(MyBookingsEvent.RatePressed(bookingWithSlot)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showCancelSheet && state.selectedBooking != null) {
        CancelBookingSheet(
            bookingWithSlot = state.selectedBooking!!,
            onDismiss = { viewModel.onEvent(MyBookingsEvent.DismissSheet) },
            onConfirmed = { viewModel.onEvent(MyBookingsEvent.CancelConfirmed(it)) }
        )
    }

    if (state.showTransferSheet && state.selectedBooking != null) {
        TransferBookingSheet(
            bookingWithSlot = state.selectedBooking!!,
            onDismiss = { viewModel.onEvent(MyBookingsEvent.DismissSheet) },
            onConfirmed = { viewModel.onEvent(MyBookingsEvent.DismissSheet) }
        )
    }

    if (state.showRateSheet && state.selectedBooking != null) {
        RateChefSheet(
            bookingWithSlot = state.selectedBooking!!,
            onDismiss = { viewModel.onEvent(MyBookingsEvent.DismissSheet) },
            onConfirmed = { viewModel.onEvent(MyBookingsEvent.DismissSheet) }
        )
    }
}
