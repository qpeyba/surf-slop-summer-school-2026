package com.qpeyba.surf_slop_summer_school_2026.domain.model

enum class EquipmentType(val apiValue: String) {
    OWN("Своя"),
    RENTAL("Прокат");

    companion object {
        fun fromApiValue(value: String): EquipmentType = entries.first { it.apiValue == value }
    }
}
