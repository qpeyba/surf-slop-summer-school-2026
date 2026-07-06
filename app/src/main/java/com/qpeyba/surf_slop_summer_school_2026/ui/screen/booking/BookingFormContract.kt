package com.qpeyba.surf_slop_summer_school_2026.ui.screen.booking

import com.qpeyba.surf_slop_summer_school_2026.domain.model.EquipmentType
import com.qpeyba.surf_slop_summer_school_2026.domain.model.Slot
import com.qpeyba.surf_slop_summer_school_2026.domain.model.User

data class BookingFormState(
    val slot: Slot? = null,
    val profile: User? = null,
    val equipment: EquipmentType = EquipmentType.RENTAL,
    val isSubmitting: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val totalPrice: Long = 0
)

sealed interface BookingFormEvent {
    data class SelectEquipment(val equipment: EquipmentType) : BookingFormEvent
    data object ConfirmPressed : BookingFormEvent
}

sealed interface BookingFormEffect {
    data class ShowSuccess(val bookingId: String) : BookingFormEffect
    data class ShowError(val message: String) : BookingFormEffect
}
