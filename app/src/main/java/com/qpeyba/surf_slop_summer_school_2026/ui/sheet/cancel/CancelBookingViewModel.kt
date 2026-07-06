package com.qpeyba.surf_slop_summer_school_2026.ui.sheet.cancel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qpeyba.surf_slop_summer_school_2026.domain.usecase.bookings.CancelBookingUseCase
import com.qpeyba.surf_slop_summer_school_2026.util.CancellationCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CancelBookingViewModel @Inject constructor(
    private val cancelBookingUseCase: CancelBookingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CancelBookingState(
        bookingWithSlot = null!!
    ))
    val state = _state.asStateFlow()

    fun init(bookingWithSlot: com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingWithSlot) {
        val info = CancellationCalculator.calculate(
            slotDateTime = bookingWithSlot.slot.dateTime,
            price = bookingWithSlot.slot.price
        )
        _state.value = CancelBookingState(
            bookingWithSlot = bookingWithSlot,
            cancellationInfo = info,
            isProcessing = false
        )
    }

    fun onEvent(event: CancelBookingEvent) {
        when (event) {
            is CancelBookingEvent.ConfirmPressed -> {
                viewModelScope.launch {
                    _state.value = _state.value.copy(isProcessing = true)
                    cancelBookingUseCase(_state.value.bookingWithSlot.booking.id)
                    _state.value = _state.value.copy(isProcessing = false)
                }
            }
            is CancelBookingEvent.KeepPressed -> { }
        }
    }
}
