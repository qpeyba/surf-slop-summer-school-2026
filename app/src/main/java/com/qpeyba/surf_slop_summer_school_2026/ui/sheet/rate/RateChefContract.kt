package com.qpeyba.surf_slop_summer_school_2026.ui.sheet.rate

import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingWithSlot

data class RateChefState(
    val booking: BookingWithSlot,
    val rating: Int = 0,
    val text: String = "",
    val isSubmitting: Boolean = false,
    val isEditing: Boolean = false
)

sealed interface RateChefEvent {
    data class RatingChanged(val rating: Int) : RateChefEvent
    data class TextChanged(val text: String) : RateChefEvent
    data object SubmitPressed : RateChefEvent
}
