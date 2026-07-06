package com.qpeyba.surf_slop_summer_school_2026.ui.sheet.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qpeyba.surf_slop_summer_school_2026.domain.usecase.bookings.TransferBookingUseCase
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
class TransferBookingViewModel @Inject constructor(
    private val getSlotsUseCase: GetSlotsUseCase,
    private val transferBookingUseCase: TransferBookingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TransferBookingState(
        oldBooking = null!!
    ))
    val state = _state.asStateFlow()

    fun init(bookingWithSlot: com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingWithSlot) {
        _state.value = TransferBookingState(oldBooking = bookingWithSlot)
        loadAvailableSlots(bookingWithSlot)
    }

    fun onEvent(event: TransferBookingEvent) {
        when (event) {
            is TransferBookingEvent.SlotSelected -> {
                _state.value = _state.value.copy(
                    selectedSlot = event.slot,
                    step = TransferStep.CONFIRM
                )
            }
            is TransferBookingEvent.BackPressed -> {
                _state.value = _state.value.copy(step = TransferStep.SELECT, selectedSlot = null)
            }
            is TransferBookingEvent.ConfirmPressed -> {
                viewModelScope.launch {
                    val bookingId = _state.value.oldBooking.booking.id
                    val newSlotId = _state.value.selectedSlot?.id ?: return@launch
                    _state.value = _state.value.copy(isProcessing = true)
                    transferBookingUseCase(bookingId, newSlotId)
                    _state.value = _state.value.copy(isProcessing = false)
                }
            }
            is TransferBookingEvent.DismissPressed -> { }
        }
    }

    private fun loadAvailableSlots(bookingWithSlot: com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingWithSlot) {
        viewModelScope.launch {
            val from = LocalDate.now()
            val to = from.plusDays(Constants.DATE_RANGE_DAYS.toLong())
            val result = getSlotsUseCase(from, to)
            result.fold(
                onSuccess = { paginated ->
                    val filtered = paginated.items.filter { slot ->
                        slot.id != bookingWithSlot.slot.id &&
                        slot.status == com.qpeyba.surf_slop_summer_school_2026.domain.model.SlotStatus.ACTIVE &&
                        slot.bookedCount < slot.capacity
                    }
                    _state.value = _state.value.copy(
                        availableSlots = if (filtered.isEmpty()) UiState.Empty else UiState.Success(filtered)
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        availableSlots = UiState.Error(e.message ?: "Ошибка загрузки")
                    )
                }
            )
        }
    }
}
