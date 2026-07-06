package com.qpeyba.surf_slop_summer_school_2026.domain.model

data class User(
    val id: String,
    val phone: String,
    val allergies: List<String>,
    val loyaltyPoints: Int,
    val loyaltyStatus: String,
    val ownEquipment: Boolean
)
