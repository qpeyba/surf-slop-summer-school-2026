package com.qpeyba.surf_slop_summer_school_2026.ui.screen.booking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.qpeyba.surf_slop_summer_school_2026.domain.model.EquipmentType
import com.qpeyba.surf_slop_summer_school_2026.domain.usecase.bookings.CreateBookingUseCase
import com.qpeyba.surf_slop_summer_school_2026.domain.usecase.profile.GetProfileUseCase
import com.qpeyba.surf_slop_summer_school_2026.domain.usecase.slots.GetSlotDetailUseCase
import com.qpeyba.surf_slop_summer_school_2026.ui.navigation.Route
import com.qpeyba.surf_slop_summer_school_2026.util.IdempotencyKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSlotDetailUseCase: GetSlotDetailUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val createBookingUseCase: CreateBookingUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Route.BookingForm>()
    private val slotId = route.slotId

    private val _state = MutableStateFlow(BookingFormState())
    val state = _state.asStateFlow()

    private val _effect = Channel<BookingFormEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadData()
    }

    fun onEvent(event: BookingFormEvent) {
        when (event) {
            is BookingFormEvent.SelectEquipment -> {
                _state.value = _state.value.copy(equipment = event.equipment)
            }
            is BookingFormEvent.ConfirmPressed -> createBooking()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            val slotResult = getSlotDetailUseCase(slotId)
            val profileResult = getProfileUseCase()

            slotResult.fold(
                onSuccess = { slot ->
                    profileResult.fold(
                        onSuccess = { profile ->
                            val equipment = if (profile.ownEquipment) EquipmentType.OWN else EquipmentType.RENTAL
                            _state.value = BookingFormState(
                                slot = slot,
                                profile = profile,
                                equipment = equipment,
                                isLoading = false,
                                totalPrice = slot.price
                            )
                        },
                        onFailure = {
                            _state.value = _state.value.copy(isLoading = false, error = "Ошибка загрузки профиля")
                        }
                    )
                },
                onFailure = {
                    _state.value = _state.value.copy(isLoading = false, error = "Ошибка загрузки класса")
                }
            )
        }
    }

    private fun createBooking() {
        val slot = _state.value.slot ?: return
        val equipment = _state.value.equipment
        val idempotencyKey = IdempotencyKey.generate()

        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, error = null)
            val result = createBookingUseCase(slot.id, equipment, idempotencyKey)
            result.fold(
                onSuccess = { booking ->
                    _state.value = _state.value.copy(isSubmitting = false)
                    _effect.send(BookingFormEffect.ShowSuccess(booking.id))
                },
                onFailure = { e ->
                    val errorMsg = when (e.message) {
                        "double_booking" -> "Вы уже записаны на этот класс"
                        "slot_full" -> "Мест больше нет"
                        "slot_cancelled" -> "Класс отменён"
                        "slot_started" -> "Класс уже начался"
                        else -> "Ошибка бронирования"
                    }
                    _state.value = _state.value.copy(isSubmitting = false, error = errorMsg)
                    _effect.send(BookingFormEffect.ShowError(errorMsg))
                }
            )
        }
    }
}
