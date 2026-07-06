package com.qpeyba.surf_slop_summer_school_2026.domain.model

data class Instructor(
    val id: String,
    val name: String,
    val status: InstructorStatus,
    val rating: Double,
    val specialization: String
)
