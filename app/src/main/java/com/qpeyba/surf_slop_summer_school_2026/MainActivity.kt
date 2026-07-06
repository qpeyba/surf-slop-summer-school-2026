package com.qpeyba.surf_slop_summer_school_2026

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.qpeyba.surf_slop_summer_school_2026.domain.usecase.auth.CheckAuthUseCase
import com.qpeyba.surf_slop_summer_school_2026.ui.navigation.ChefTableNavHost
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.ChefTableTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var checkAuthUseCase: CheckAuthUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChefTableTheme {
                ChefTableNavHost(checkAuthUseCase = checkAuthUseCase)
            }
        }
    }
}
