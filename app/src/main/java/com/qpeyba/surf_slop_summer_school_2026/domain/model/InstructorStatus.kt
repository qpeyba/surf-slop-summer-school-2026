package com.qpeyba.surf_slop_summer_school_2026.domain.model

enum class InstructorStatus(val apiValue: String) {
    PERMANENT("Постоянный"),
    GUEST("Приглашённый");

    companion object {
        fun fromApiValue(value: String): InstructorStatus = entries.first { it.apiValue == value }
    }
}
