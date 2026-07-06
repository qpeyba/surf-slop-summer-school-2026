package com.qpeyba.surf_slop_summer_school_2026.domain.usecase.auth

import com.qpeyba.surf_slop_summer_school_2026.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String, code: String): Result<String> {
        return authRepository.verifyOtp(phone, code)
    }
}
