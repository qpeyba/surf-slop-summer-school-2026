package com.qpeyba.surf_slop_summer_school_2026.ui.sheet.rate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qpeyba.surf_slop_summer_school_2026.domain.usecase.bookings.SubmitReviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RateChefViewModel @Inject constructor(
    private val submitReviewUseCase: SubmitReviewUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RateChefState(
        booking = null!!
    ))
    val state = _state.asStateFlow()

    fun init(bookingWithSlot: com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingWithSlot) {
        val isEditing = bookingWithSlot.booking.reviewRating != null
        _state.value = RateChefState(
            booking = bookingWithSlot,
            rating = bookingWithSlot.booking.reviewRating ?: 0,
            text = bookingWithSlot.booking.reviewText ?: "",
            isEditing = isEditing
        )
    }

    fun onEvent(event: RateChefEvent) {
        when (event) {
            is RateChefEvent.RatingChanged -> {
                _state.value = _state.value.copy(rating = event.rating)
            }
            is RateChefEvent.TextChanged -> {
                if (event.text.length <= 500) {
                    _state.value = _state.value.copy(text = event.text)
                }
            }
            is RateChefEvent.SubmitPressed -> {
                viewModelScope.launch {
                    _state.value = _state.value.copy(isSubmitting = true)
                    val bookingId = _state.value.booking.booking.id
                    submitReviewUseCase(bookingId, _state.value.rating, _state.value.text.ifBlank { null })
                    _state.value = _state.value.copy(isSubmitting = false)
                }
            }
        }
    }
}
