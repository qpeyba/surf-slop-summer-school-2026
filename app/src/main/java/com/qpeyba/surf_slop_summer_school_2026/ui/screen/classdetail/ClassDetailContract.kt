package com.qpeyba.surf_slop_summer_school_2026.ui.screen.classdetail

import com.qpeyba.surf_slop_summer_school_2026.domain.model.Slot
import com.qpeyba.surf_slop_summer_school_2026.util.UiState
import java.time.LocalDateTime

data class ClassDetailState(
    val slot: UiState<Slot> = UiState.Loading,
    val isBookingAvailable: Boolean = false,
    val ctaText: String = ""
)

sealed interface ClassDetailEvent {
    data class BookPressed(val slotId: String) : ClassDetailEvent
    data object BackPressed : ClassDetailEvent
}
