package com.qpeyba.surf_slop_summer_school_2026.domain.model

import java.time.LocalDateTime

data class Booking(
    val id: String,
    val slotId: String,
    val equipmentType: EquipmentType,
    val status: BookingStatus,
    val refundAmount: Long?,
    val reviewRating: Int?,
    val reviewText: String?,
    val createdAt: LocalDateTime,
    val cancelledAt: LocalDateTime?
)
