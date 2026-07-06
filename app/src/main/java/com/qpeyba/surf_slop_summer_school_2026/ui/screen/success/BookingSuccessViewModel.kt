package com.qpeyba.surf_slop_summer_school_2026.ui.screen.success

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.qpeyba.surf_slop_summer_school_2026.domain.usecase.bookings.GetBookingDetailUseCase
import com.qpeyba.surf_slop_summer_school_2026.ui.navigation.Route
import com.qpeyba.surf_slop_summer_school_2026.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingSuccessViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getBookingDetailUseCase: GetBookingDetailUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Route.BookingSuccess>()
    private val bookingId = route.bookingId

    private val _state = MutableStateFlow(BookingSuccessState())
    val state = _state.asStateFlow()

    init {
        loadBooking()
    }

    private fun loadBooking() {
        viewModelScope.launch {
            _state.value = BookingSuccessState(booking = UiState.Loading)
            val result = getBookingDetailUseCase(bookingId)
            result.fold(
                onSuccess = { bookingWithSlot ->
                    _state.value = BookingSuccessState(booking = UiState.Success(bookingWithSlot))
                },
                onFailure = { e ->
                    _state.value = BookingSuccessState(booking = UiState.Error(e.message ?: "Ошибка загрузки"))
                }
            )
        }
    }
}
