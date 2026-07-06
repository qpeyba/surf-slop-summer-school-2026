package com.qpeyba.surf_slop_summer_school_2026.ui.screen.success

import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingWithSlot
import com.qpeyba.surf_slop_summer_school_2026.util.UiState

data class BookingSuccessState(
    val booking: UiState<BookingWithSlot> = UiState.Loading
)
