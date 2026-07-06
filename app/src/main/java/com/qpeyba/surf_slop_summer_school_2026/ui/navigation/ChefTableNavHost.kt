package com.qpeyba.surf_slop_summer_school_2026.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.qpeyba.surf_slop_summer_school_2026.domain.usecase.auth.CheckAuthUseCase
import com.qpeyba.surf_slop_summer_school_2026.ui.screen.auth.AuthScreen
import com.qpeyba.surf_slop_summer_school_2026.ui.screen.booking.BookingFormScreen
import com.qpeyba.surf_slop_summer_school_2026.ui.screen.classdetail.ClassDetailScreen
import com.qpeyba.surf_slop_summer_school_2026.ui.screen.mybookings.MyBookingsScreen
import com.qpeyba.surf_slop_summer_school_2026.ui.screen.profile.ProfileScreen
import com.qpeyba.surf_slop_summer_school_2026.ui.screen.schedule.ScheduleScreen
import com.qpeyba.surf_slop_summer_school_2026.ui.screen.success.BookingSuccessScreen

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Route
)

private val bottomNavItems = listOf(
    BottomNavItem("Расписание", Icons.Default.CalendarMonth, Route.Schedule),
    BottomNavItem("Мои брони", Icons.Default.Receipt, Route.MyBookings),
    BottomNavItem("Профиль", Icons.Default.Person, Route.Profile)
)

@Composable
fun ChefTableNavHost(
    checkAuthUseCase: CheckAuthUseCase
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var isAuthorized by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        isAuthorized = checkAuthUseCase()
    }

    if (isAuthorized == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val showBottomBar = currentDestination?.let { dest ->
        dest.hasRoute<Route.Schedule>() ||
        dest.hasRoute<Route.MyBookings>() ||
        dest.hasRoute<Route.Profile>()
    } ?: true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hasRoute(item.route::class) ?: false
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isAuthorized == true) Route.Schedule else Route.Auth,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable<Route.Auth> {
                AuthScreen(
                    onLoginSuccess = {
                        navController.navigate(Route.Schedule) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable<Route.Schedule> {
                ScheduleScreen(
                    onClassClick = { slotId ->
                        navController.navigate(Route.ClassDetail(slotId))
                    }
                )
            }
            composable<Route.ClassDetail> {
                ClassDetailScreen(
                    onBook = { slotId ->
                        navController.navigate(Route.BookingForm(slotId))
                    }
                )
            }
            composable<Route.BookingForm> {
                BookingFormScreen(
                    onSuccess = { bookingId ->
                        navController.navigate(Route.BookingSuccess(bookingId)) {
                            popUpTo(Route.Schedule) { inclusive = false }
                        }
                    }
                )
            }
            composable<Route.BookingSuccess> {
                BookingSuccessScreen(
                    onNavigateToMyBookings = {
                        navController.navigate(Route.MyBookings) {
                            popUpTo(Route.Schedule) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSchedule = {
                        navController.navigate(Route.Schedule) {
                            popUpTo(Route.Schedule) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable<Route.MyBookings> {
                MyBookingsScreen()
            }
            composable<Route.Profile> {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Route.Auth) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
