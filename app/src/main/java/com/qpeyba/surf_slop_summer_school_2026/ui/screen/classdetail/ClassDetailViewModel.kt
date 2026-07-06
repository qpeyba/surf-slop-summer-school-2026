package com.qpeyba.surf_slop_summer_school_2026.ui.screen.classdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.qpeyba.surf_slop_summer_school_2026.domain.model.SlotStatus
import com.qpeyba.surf_slop_summer_school_2026.domain.usecase.slots.GetSlotDetailUseCase
import com.qpeyba.surf_slop_summer_school_2026.ui.navigation.Route
import com.qpeyba.surf_slop_summer_school_2026.util.Constants
import com.qpeyba.surf_slop_summer_school_2026.util.PriceFormatter
import com.qpeyba.surf_slop_summer_school_2026.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ClassDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSlotDetailUseCase: GetSlotDetailUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Route.ClassDetail>()
    private val slotId = route.slotId

    private val _state = MutableStateFlow(ClassDetailState())
    val state = _state.asStateFlow()

    init {
        loadSlot()
    }

    fun onEvent(event: ClassDetailEvent) { }

    private fun loadSlot() {
        viewModelScope.launch {
            val result = getSlotDetailUseCase(slotId)
            result.fold(
                onSuccess = { slot ->
                    val now = java.time.LocalDateTime.now()
                    val isBeforeStart = now.isBefore(slot.dateTime.minusMinutes(Constants.MIN_BOOKING_MINUTES_BEFORE.toLong()))
                    val hasCapacity = slot.bookedCount < slot.capacity
                    val isActive = slot.status == SlotStatus.ACTIVE

                    val ctaText = when {
                        slot.status == SlotStatus.CANCELLED_BY_STUDIO -> "Отменён"
                        !hasCapacity -> "Нет мест"
                        !isBeforeStart -> "Скоро начнётся"
                        isActive -> "Забронировать — ${PriceFormatter.format(slot.price)}"
                        else -> "Недоступно"
                    }
                    val isAvailable = isActive && hasCapacity && isBeforeStart

                    _state.value = ClassDetailState(
                        slot = UiState.Success(slot),
                        isBookingAvailable = isAvailable,
                        ctaText = ctaText
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        slot = UiState.Error(e.message ?: "Ошибка загрузки")
                    )
                }
            )
        }
    }
}
