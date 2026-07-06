package com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val id: String,
    val phone: String,
    val allergies: List<String> = emptyList(),
    val loyaltyPoints: Int,
    val loyaltyStatus: String? = null,
    val ownEquipment: Boolean
)
