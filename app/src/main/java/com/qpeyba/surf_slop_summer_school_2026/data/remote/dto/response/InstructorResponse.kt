package com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class InstructorResponse(
    val id: String,
    val name: String,
    val status: String,
    val rating: Double,
    val specialization: String
)
