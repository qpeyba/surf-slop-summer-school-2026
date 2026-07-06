package com.qpeyba.surf_slop_summer_school_2026.domain.model

data class PaginatedResult<T>(
    val items: List<T>,
    val total: Int
)
