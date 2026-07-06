package com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class ReviewRequest(
    val rating: Int,
    val text: String? = null
)
