package com.qpeyba.surf_slop_summer_school_2026.domain.model

enum class SlotStatus(val apiValue: String) {
    ACTIVE("Активен"),
    CANCELLED_BY_STUDIO("ОтменёнСтудией");

    companion object {
        fun fromApiValue(value: String): SlotStatus = entries.first { it.apiValue == value }
    }
}
