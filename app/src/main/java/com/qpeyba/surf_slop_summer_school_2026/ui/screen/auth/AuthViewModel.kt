package com.qpeyba.surf_slop_summer_school_2026.ui.screen.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qpeyba.surf_slop_summer_school_2026.domain.usecase.auth.RequestOtpUseCase
import com.qpeyba.surf_slop_summer_school_2026.domain.usecase.auth.VerifyOtpUseCase
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.AuthRepository
import com.qpeyba.surf_slop_summer_school_2026.util.Constants
import com.qpeyba.surf_slop_summer_school_2026.util.PhoneMask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val requestOtpUseCase: RequestOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AuthEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.PhoneChanged -> {
                _state.value = _state.value.copy(phone = event.phone, error = null)
            }
            is AuthEvent.GetCodePressed -> requestOtp()
            is AuthEvent.CodeChanged -> {
                _state.value = _state.value.copy(code = event.code, error = null)
            }
            is AuthEvent.VerifyCodePressed -> verifyOtp()
            is AuthEvent.ResendCodePressed -> {
                if (_state.value.isResendAvailable) {
                    requestOtp()
                }
            }
        }
    }

    private fun requestOtp() {
        val phone = PhoneMask.toE164(_state.value.phone)
        if (!PhoneMask.isValid(_state.value.phone)) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = requestOtpUseCase(phone)
            result.fold(
                onSuccess = { message ->
                    if (message == "admin_bypass") {
                        _state.value = _state.value.copy(isLoading = false)
                        _effect.send(AuthEffect.NavigateToSchedule)
                        return@launch
                    }
                    val devCode = extractDevCode(message)
                    if (devCode != null) {
                        Log.d("OTP_DEV", "Dev code: $devCode")
                    }
                    _state.value = _state.value.copy(
                        step = AuthStep.Otp,
                        isLoading = false,
                        isResendAvailable = false,
                        resendSecondsLeft = Constants.OTP_RESEND_SECONDS,
                        devCode = devCode,
                        code = ""
                    )
                    startResendTimer()
                },
                onFailure = { e ->
                    val error = if (e.message?.contains("429") == true) {
                        "Подождите 1 минуту"
                    } else {
                        "Ошибка сети, попробуйте снова"
                    }
                    _state.value = _state.value.copy(isLoading = false, error = error)
                }
            )
        }
    }

    private fun extractDevCode(message: String): String? {
        val regex = Regex("""\(dev:\s*(\d+)\)""")
        return regex.find(message)?.groupValues?.getOrNull(1)
    }

    private fun verifyOtp() {
        val phone = PhoneMask.toE164(_state.value.phone)
        val code = _state.value.code
        if (code.length != 6) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = verifyOtpUseCase(phone, code)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(isLoading = false)
                    _effect.send(AuthEffect.NavigateToSchedule)
                },
                onFailure = { e ->
                    val msg = e.message ?: ""
                    when {
                        msg.contains("invalid_code") -> {
                            val attemptsLeft = _state.value.otpAttemptsLeft - 1
                            if (attemptsLeft <= 0) {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    error = "Слишком много неверных попыток. Подождите ${Constants.OTP_BLOCK_SECONDS} секунд.",
                                    isOtpBlocked = true,
                                    code = ""
                                )
                                startBlockTimer()
                            } else {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    error = "Неверный код, попробуйте снова (осталось $attemptsLeft попыток)",
                                    code = "",
                                    otpAttemptsLeft = attemptsLeft
                                )
                            }
                        }
                        msg.contains("too_many_requests") -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = "Слишком много запросов. Подождите ${Constants.OTP_BLOCK_SECONDS} секунд.",
                                isOtpBlocked = true
                            )
                            startBlockTimer()
                        }
                        else -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = "Ошибка сети, попробуйте снова"
                            )
                        }
                    }
                }
            )
        }
    }

    private fun startResendTimer() {
        viewModelScope.launch {
            for (i in Constants.OTP_RESEND_SECONDS downTo 1) {
                _state.value = _state.value.copy(resendSecondsLeft = i)
                delay(1000L)
            }
            _state.value = _state.value.copy(isResendAvailable = true)
        }
    }

    private fun startBlockTimer() {
        viewModelScope.launch {
            delay(Constants.OTP_BLOCK_SECONDS * 1000L)
            _state.value = _state.value.copy(
                isOtpBlocked = false,
                otpAttemptsLeft = Constants.OTP_MAX_ATTEMPTS,
                error = null
            )
        }
    }
}
