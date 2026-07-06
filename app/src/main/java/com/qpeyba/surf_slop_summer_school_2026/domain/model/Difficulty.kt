package com.qpeyba.surf_slop_summer_school_2026.domain.model

enum class Difficulty(val apiValue: String) {
    BEGINNER("Для новичков"),
    EXPERIENCED("Для опытных");

    companion object {
        fun fromApiValue(value: String): Difficulty = entries.first { it.apiValue == value }
    }
}
