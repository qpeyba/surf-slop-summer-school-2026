package com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class SlotPageResponse(
    val items: List<SlotResponse>,
    val total: Int
)
