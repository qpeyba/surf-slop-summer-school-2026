package com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class OtpVerifyRequest(
    val phone: String,
    val code: String
)
