package com.qpeyba.surf_slop_summer_school_2026.domain.model

enum class BookingStatus(val apiValue: String) {
    ACTIVE("Активна"),
    CANCELLED_BY_CLIENT("ОтмененаКлиентом"),
    CANCELLED_BY_STUDIO("ОтмененаСтудией"),
    COMPLETED("Завершена"),
    CLIENT_NO_SHOW("КлиентНеПришёл");

    companion object {
        fun fromApiValue(value: String): BookingStatus = entries.first { it.apiValue == value }
    }
}
