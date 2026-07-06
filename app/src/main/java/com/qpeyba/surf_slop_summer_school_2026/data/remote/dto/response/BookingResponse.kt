package com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class BookingResponse(
    val id: String,
    val slotId: String,
    val equipmentType: String,
    val status: String,
    val refundAmount: Long? = null,
    val reviewRating: Int? = null,
    val reviewText: String? = null,
    val createdAt: String,
    val cancelledAt: String? = null,
    val slot: SlotResponse? = null
)
