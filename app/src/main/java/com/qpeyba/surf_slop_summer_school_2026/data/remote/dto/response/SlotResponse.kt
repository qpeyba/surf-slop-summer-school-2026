package com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class SlotResponse(
    val id: String,
    val dateTime: String,
    val menu: String? = null,
    val photoUrls: List<String> = emptyList(),
    val difficulty: String,
    val price: Long,
    val address: String,
    val capacity: Int,
    val bookedCount: Int,
    val status: String,
    val instructor: InstructorResponse? = null
)
