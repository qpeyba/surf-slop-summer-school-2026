package com.qpeyba.surf_slop_summer_school_2026.domain.model

import java.time.LocalDateTime

data class Slot(
    val id: String,
    val dateTime: LocalDateTime,
    val menu: String?,
    val photoUrls: List<String>,
    val difficulty: Difficulty,
    val price: Long,
    val address: String,
    val capacity: Int,
    val bookedCount: Int,
    val status: SlotStatus,
    val instructor: Instructor?
)
