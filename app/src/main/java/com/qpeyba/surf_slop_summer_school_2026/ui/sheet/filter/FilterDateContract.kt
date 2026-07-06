package com.qpeyba.surf_slop_summer_school_2026.ui.sheet.filter

import java.time.LocalDate

data class FilterDateState(
    val selectedDate: LocalDate? = null,
    val quickFilter: QuickFilter? = null
)

enum class QuickFilter {
    TODAY,
    TOMORROW,
    THIS_WEEK,
    NEXT_WEEK
}

sealed interface FilterDateEvent {
    data class DateSelected(val date: LocalDate) : FilterDateEvent
    data class QuickFilterSelected(val filter: QuickFilter) : FilterDateEvent
    data object ApplyPressed : FilterDateEvent
    data object ResetPressed : FilterDateEvent
    data object DismissPressed : FilterDateEvent
}
