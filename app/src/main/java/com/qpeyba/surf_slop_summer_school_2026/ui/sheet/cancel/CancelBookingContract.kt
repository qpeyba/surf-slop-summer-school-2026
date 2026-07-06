package com.qpeyba.surf_slop_summer_school_2026.ui.sheet.cancel

import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingWithSlot
import com.qpeyba.surf_slop_summer_school_2026.domain.model.CancellationInfo

data class CancelBookingState(
    val bookingWithSlot: BookingWithSlot? = null,
    val cancellationInfo: CancellationInfo? = null,
    val isProcessing: Boolean = false
)

sealed interface CancelBookingEvent {
    data object ConfirmPressed : CancelBookingEvent
    data object KeepPressed : CancelBookingEvent
}
