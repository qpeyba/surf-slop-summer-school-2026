package com.qpeyba.surf_slop_summer_school_2026.ui.screen.profile

import com.qpeyba.surf_slop_summer_school_2026.domain.model.EquipmentType
import com.qpeyba.surf_slop_summer_school_2026.domain.model.User
import com.qpeyba.surf_slop_summer_school_2026.util.UiState

data class ProfileState(
    val user: UiState<User> = UiState.Loading,
    val allergiesInput: String = "",
    val isUpdating: Boolean = false
)

sealed interface ProfileEvent {
    data class AddAllergy(val allergy: String) : ProfileEvent
    data class RemoveAllergy(val allergy: String) : ProfileEvent
    data class EquipmentChanged(val equipment: EquipmentType) : ProfileEvent
    data object MyBookingsPressed : ProfileEvent
    data object LogoutPressed : ProfileEvent
}
