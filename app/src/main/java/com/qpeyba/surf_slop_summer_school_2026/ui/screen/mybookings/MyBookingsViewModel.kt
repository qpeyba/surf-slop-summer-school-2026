package com.qpeyba.surf_slop_summer_school_2026.ui.screen.mybookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingStatus
import com.qpeyba.surf_slop_summer_school_2026.domain.usecase.bookings.CancelBookingUseCase
import com.qpeyba.surf_slop_summer_school_2026.domain.usecase.bookings.GetBookingsUseCase
import com.qpeyba.surf_slop_summer_school_2026.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyBookingsViewModel @Inject constructor(
    private val getBookingsUseCase: GetBookingsUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MyBookingsState())
    val state = _state.asStateFlow()

    init {
        loadBookings()
    }

    fun onEvent(event: MyBookingsEvent) {
        when (event) {
            is MyBookingsEvent.Refresh -> {
                _state.value = _state.value.copy(isRefreshing = true)
                loadBookings()
            }
            is MyBookingsEvent.CancelPressed -> {
                _state.value = _state.value.copy(
                    showCancelSheet = true,
                    selectedBooking = event.booking
                )
            }
            is MyBookingsEvent.TransferPressed -> {
                _state.value = _state.value.copy(
                    showTransferSheet = true,
                    selectedBooking = event.booking
                )
            }
            is MyBookingsEvent.RatePressed -> {
                _state.value = _state.value.copy(
                    showRateSheet = true,
                    selectedBooking = event.booking
                )
            }
            is MyBookingsEvent.BookingClicked -> { }
            is MyBookingsEvent.DismissSheet -> {
                _state.value = _state.value.copy(
                    showCancelSheet = false,
                    showTransferSheet = false,
                    showRateSheet = false,
                    selectedBooking = null
                )
            }
            is MyBookingsEvent.CancelConfirmed -> {
                viewModelScope.launch {
                    cancelBookingUseCase(event.bookingId)
                    loadBookings()
                }
                _state.value = _state.value.copy(showCancelSheet = false, selectedBooking = null)
            }
        }
    }

    private fun loadBookings() {
        viewModelScope.launch {
            val result = getBookingsUseCase(expand = "slot")
            result.fold(
                onSuccess = { paginated ->
                    val sections = paginated.items.groupBy { item ->
                        val status = item.booking.status
                        when (status) {
                            BookingStatus.ACTIVE -> BookingStatus.ACTIVE
                            BookingStatus.CANCELLED_BY_CLIENT,
                            BookingStatus.CANCELLED_BY_STUDIO,
                            BookingStatus.CLIENT_NO_SHOW -> BookingStatus.CANCELLED_BY_CLIENT
                            BookingStatus.COMPLETED -> BookingStatus.COMPLETED
                        }
                    }
                    val uiState = if (paginated.items.isEmpty()) UiState.Empty else UiState.Success(paginated.items)
                    _state.value = _state.value.copy(
                        bookings = uiState,
                        sections = sections,
                        isRefreshing = false
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        bookings = UiState.Error(e.message ?: "Ошибка загрузки"),
                        isRefreshing = false
                    )
                }
            )
        }
    }
}
