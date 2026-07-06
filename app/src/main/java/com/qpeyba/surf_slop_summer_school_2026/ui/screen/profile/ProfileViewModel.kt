package com.qpeyba.surf_slop_summer_school_2026.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qpeyba.surf_slop_summer_school_2026.domain.model.EquipmentType
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.AuthRepository
import com.qpeyba.surf_slop_summer_school_2026.domain.usecase.auth.LogoutUseCase
import com.qpeyba.surf_slop_summer_school_2026.domain.usecase.profile.GetProfileUseCase
import com.qpeyba.surf_slop_summer_school_2026.domain.usecase.profile.UpdateProfileUseCase
import com.qpeyba.surf_slop_summer_school_2026.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    init {
        loadProfile()
        loadAdminStatus()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.AddAllergy -> {
                if (event.allergy.isNotBlank()) {
                    val current = (_state.value.user as? UiState.Success)?.data?.allergies ?: emptyList()
                    val updated = current + event.allergy
                    updateAllergies(updated)
                }
            }
            is ProfileEvent.RemoveAllergy -> {
                val current = (_state.value.user as? UiState.Success)?.data?.allergies ?: emptyList()
                val updated = current - event.allergy
                updateAllergies(updated)
            }
            is ProfileEvent.EquipmentChanged -> {
                updateProfile(allergies = null, ownEquipment = event.equipment == EquipmentType.OWN)
            }
            is ProfileEvent.MyBookingsPressed -> { }
            is ProfileEvent.LogoutPressed -> {
                viewModelScope.launch {
                    logoutUseCase()
                }
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val result = getProfileUseCase()
            result.fold(
                onSuccess = { user ->
                    _state.value = _state.value.copy(user = UiState.Success(user))
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(user = UiState.Error(e.message ?: "Ошибка загрузки"))
                }
            )
        }
    }

    private fun updateAllergies(allergies: List<String>) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUpdating = true)
            updateProfileUseCase(allergies = allergies, ownEquipment = null)
            loadProfile()
            _state.value = _state.value.copy(isUpdating = false)
        }
    }

    private fun updateProfile(allergies: List<String>? = null, ownEquipment: Boolean? = null) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUpdating = true)
            updateProfileUseCase(allergies = allergies, ownEquipment = ownEquipment)
            loadProfile()
            _state.value = _state.value.copy(isUpdating = false)
        }
    }

    private fun loadAdminStatus() {
        viewModelScope.launch {
            val isAdmin = authRepository.isAdmin()
            _state.value = _state.value.copy(isAdmin = isAdmin)
        }
    }
}
