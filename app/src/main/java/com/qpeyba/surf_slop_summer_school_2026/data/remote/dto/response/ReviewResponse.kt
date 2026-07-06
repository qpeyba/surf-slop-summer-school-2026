package com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ReviewResponse(
    val review: ReviewData? = null,
    val instructorRating: Double? = null
)

@Serializable
data class ReviewData(
    val rating: Int,
    val text: String? = null
)
