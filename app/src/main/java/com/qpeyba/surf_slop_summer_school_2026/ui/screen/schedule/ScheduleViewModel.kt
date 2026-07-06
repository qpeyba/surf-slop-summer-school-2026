package com.qpeyba.surf_slop_summer_school_2026.ui.screen.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qpeyba.surf_slop_summer_school_2026.domain.usecase.slots.GetSlotsUseCase
import com.qpeyba.surf_slop_summer_school_2026.util.Constants
import com.qpeyba.surf_slop_summer_school_2026.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val getSlotsUseCase: GetSlotsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduleState())
    val state = _state.asStateFlow()

    init {
        loadSlots()
    }

    fun onEvent(event: ScheduleEvent) {
        when (event) {
            is ScheduleEvent.DateSelected -> {
                _state.value = _state.value.copy(selectedDate = event.date)
                loadSlots()
            }
            is ScheduleEvent.Refresh -> {
                _state.value = _state.value.copy(isRefreshing = true)
                loadSlots()
            }
            is ScheduleEvent.ClassClicked -> { }
            is ScheduleEvent.OpenFilter -> { }
        }
    }

    private fun loadSlots() {
        viewModelScope.launch {
            val currentState = _state.value
            val from = currentState.selectedDate
            val to = currentState.selectedDate.plusDays(Constants.DATE_RANGE_DAYS.toLong())

            val result = getSlotsUseCase(from, to, Constants.PAGE_SIZE)
            result.fold(
                onSuccess = { paginated ->
                    val uiState = if (paginated.items.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(paginated.items)
                    }
                    _state.value = _state.value.copy(classes = uiState, isRefreshing = false)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        classes = UiState.Error(e.message ?: "Ошибка загрузки"),
                        isRefreshing = false
                    )
                }
            )
        }
    }
}
