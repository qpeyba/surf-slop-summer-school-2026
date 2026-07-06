package com.qpeyba.surf_slop_summer_school_2026.domain.usecase.auth

import com.qpeyba.surf_slop_summer_school_2026.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
