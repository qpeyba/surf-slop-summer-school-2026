package com.qpeyba.surf_slop_summer_school_2026.domain.model

data class CancellationInfo(
    val canCancel: Boolean,
    val hoursRemaining: Double,
    val refundPercentage: Int?,
    val refundAmount: Long?,
    val seatsReturned: Boolean,
    val warningText: String,
    val variant: CancellationVariant
)
