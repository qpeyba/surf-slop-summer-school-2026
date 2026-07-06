package com.qpeyba.surf_slop_summer_school_2026.ui.screen.auth

sealed interface AuthStep {
    data object Phone : AuthStep
    data object Otp : AuthStep
}

data class AuthState(
    val step: AuthStep = AuthStep.Phone,
    val phone: String = "",
    val code: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isResendAvailable: Boolean = false,
    val resendSecondsLeft: Int = 0,
    val otpAttemptsLeft: Int = 3,
    val isOtpBlocked: Boolean = false,
    val devCode: String? = null
)

sealed interface AuthEvent {
    data class PhoneChanged(val phone: String) : AuthEvent
    data object GetCodePressed : AuthEvent
    data class CodeChanged(val code: String) : AuthEvent
    data object ResendCodePressed : AuthEvent
    data object VerifyCodePressed : AuthEvent
}

sealed interface AuthEffect {
    data object NavigateToSchedule : AuthEffect
}
