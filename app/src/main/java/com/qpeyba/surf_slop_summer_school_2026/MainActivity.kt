package com.qpeyba.surf_slop_summer_school_2026

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.qpeyba.surf_slop_summer_school_2026.ui.navigation.ChefTableNavHost
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.ChefTableTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChefTableTheme {
                ChefTableNavHost()
            }
        }
    }
}
