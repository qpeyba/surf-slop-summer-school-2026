package com.qpeyba.surf_slop_summer_school_2026.ui.sheet.filter

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class FilterDateViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(FilterDateState())
    val state = _state.asStateFlow()

    fun onEvent(event: FilterDateEvent) {
        when (event) {
            is FilterDateEvent.DateSelected -> {
                _state.value = _state.value.copy(selectedDate = event.date, quickFilter = null)
            }
            is FilterDateEvent.QuickFilterSelected -> {
                val date = when (event.filter) {
                    QuickFilter.TODAY -> LocalDate.now()
                    QuickFilter.TOMORROW -> LocalDate.now().plusDays(1)
                    QuickFilter.THIS_WEEK -> LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
                    QuickFilter.NEXT_WEEK -> LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).plusDays(1)
                }
                _state.value = _state.value.copy(selectedDate = date, quickFilter = event.filter)
            }
            is FilterDateEvent.ResetPressed -> {
                _state.value = FilterDateState()
            }
            is FilterDateEvent.ApplyPressed -> { }
            is FilterDateEvent.DismissPressed -> { }
        }
    }
}
