package com.qpeyba.surf_slop_summer_school_2026.ui.screen.mybookings

import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingStatus
import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingWithSlot
import com.qpeyba.surf_slop_summer_school_2026.util.UiState

data class MyBookingsState(
    val bookings: UiState<List<BookingWithSlot>> = UiState.Loading,
    val sections: Map<BookingStatus, List<BookingWithSlot>> = emptyMap(),
    val isRefreshing: Boolean = false,
    val showCancelSheet: Boolean = false,
    val showTransferSheet: Boolean = false,
    val showRateSheet: Boolean = false,
    val selectedBooking: BookingWithSlot? = null
)

sealed interface MyBookingsEvent {
    data object Refresh : MyBookingsEvent
    data class CancelPressed(val booking: BookingWithSlot) : MyBookingsEvent
    data class TransferPressed(val booking: BookingWithSlot) : MyBookingsEvent
    data class RatePressed(val booking: BookingWithSlot) : MyBookingsEvent
    data class BookingClicked(val booking: BookingWithSlot) : MyBookingsEvent
    data object DismissSheet : MyBookingsEvent
    data class CancelConfirmed(val bookingId: String) : MyBookingsEvent
}
