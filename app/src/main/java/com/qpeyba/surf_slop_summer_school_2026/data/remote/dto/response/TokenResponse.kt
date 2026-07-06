package com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long
)
