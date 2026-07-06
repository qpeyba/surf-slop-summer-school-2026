package com.qpeyba.surf_slop_summer_school_2026.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable data object Auth : Route
    @Serializable data object Schedule : Route
    @Serializable data class ClassDetail(val slotId: String) : Route
    @Serializable data class BookingForm(val slotId: String) : Route
    @Serializable data class BookingSuccess(val bookingId: String) : Route
    @Serializable data object MyBookings : Route
    @Serializable data object Profile : Route
}
