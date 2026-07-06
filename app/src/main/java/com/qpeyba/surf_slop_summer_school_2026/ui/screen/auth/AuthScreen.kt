package com.qpeyba.surf_slop_summer_school_2026.ui.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefButton
import com.qpeyba.surf_slop_summer_school_2026.ui.components.CountdownTimer
import com.qpeyba.surf_slop_summer_school_2026.ui.components.OtpInput
import com.qpeyba.surf_slop_summer_school_2026.ui.components.PhoneInput
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Terracotta
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextSecondary
import com.qpeyba.surf_slop_summer_school_2026.util.Constants
import com.qpeyba.surf_slop_summer_school_2026.util.PhoneMask

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.NavigateToSchedule -> onLoginSuccess()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Icon(
            imageVector = Icons.Default.RestaurantMenu,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Terracotta
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Шеф-стол",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(32.dp))

        when (state.step) {
            is AuthStep.Phone -> {
                Text(
                    text = "Добро пожаловать!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Введите номер телефона для входа",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                PhoneInput(
                    value = state.phone,
                    onValueChange = { viewModel.onEvent(AuthEvent.PhoneChanged(it)) },
                    isError = state.error != null,
                    errorMessage = state.error
                )
                Spacer(modifier = Modifier.height(16.dp))

                ChefButton(
                    text = "Получить код",
                    onClick = { viewModel.onEvent(AuthEvent.GetCodePressed) },
                    enabled = PhoneMask.isValid(state.phone) && !state.isLoading,
                    isLoading = state.isLoading
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Продолжая, вы соглашаетесь с обработкой персональных данных",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
            is AuthStep.Otp -> {
                Text(
                    text = "Код отправлен на ${PhoneMask.apply(state.phone)}",
                    fontSize = 16.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                val devCode = state.devCode
                if (devCode != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Dev code: $devCode",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Terracotta,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                OtpInput(
                    code = state.code,
                    onCodeChanged = { viewModel.onEvent(AuthEvent.CodeChanged(it)) },
                    isError = state.error != null
                )
                val errorText = state.error
                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorText,
                        fontSize = 13.sp,
                        color = com.qpeyba.surf_slop_summer_school_2026.ui.theme.DestructiveRed,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (state.isResendAvailable) {
                    ChefButton(
                        text = "Отправить код повторно",
                        onClick = { viewModel.onEvent(AuthEvent.ResendCodePressed) },
                        enabled = !state.isLoading
                    )
                } else {
                    CountdownTimer(seconds = Constants.OTP_RESEND_SECONDS)
                }
            }
        }
    }
}
