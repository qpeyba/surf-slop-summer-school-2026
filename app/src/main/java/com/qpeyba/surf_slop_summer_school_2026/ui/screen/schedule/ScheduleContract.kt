package com.qpeyba.surf_slop_summer_school_2026.ui.screen.schedule

import com.qpeyba.surf_slop_summer_school_2026.domain.model.Slot
import com.qpeyba.surf_slop_summer_school_2026.util.UiState
import java.time.LocalDate

data class ScheduleState(
    val classes: UiState<List<Slot>> = UiState.Loading,
    val selectedDate: LocalDate = LocalDate.now(),
    val isRefreshing: Boolean = false
)

sealed interface ScheduleEvent {
    data class DateSelected(val date: LocalDate) : ScheduleEvent
    data object Refresh : ScheduleEvent
    data class ClassClicked(val slotId: String) : ScheduleEvent
    data object OpenFilter : ScheduleEvent
}
