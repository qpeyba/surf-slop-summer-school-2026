package com.qpeyba.surf_slop_summer_school_2026.ui.sheet.transfer

import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingWithSlot
import com.qpeyba.surf_slop_summer_school_2026.domain.model.Slot
import com.qpeyba.surf_slop_summer_school_2026.util.UiState

data class TransferBookingState(
    val step: TransferStep = TransferStep.SELECT,
    val oldBooking: BookingWithSlot,
    val availableSlots: UiState<List<Slot>> = UiState.Loading,
    val selectedSlot: Slot? = null,
    val isProcessing: Boolean = false
)

enum class TransferStep {
    SELECT,
    CONFIRM
}

sealed interface TransferBookingEvent {
    data class SlotSelected(val slot: Slot) : TransferBookingEvent
    data object ConfirmPressed : TransferBookingEvent
    data object BackPressed : TransferBookingEvent
    data object DismissPressed : TransferBookingEvent
}
