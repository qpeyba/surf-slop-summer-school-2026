package com.qpeyba.surf_slop_summer_school_2026.data.mapper

import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response.ProfileResponse
import com.qpeyba.surf_slop_summer_school_2026.domain.model.User
import javax.inject.Inject

class ProfileMapper @Inject constructor() {
    fun toDomain(response: ProfileResponse): User {
        return User(
            id = response.id,
            phone = response.phone,
            allergies = response.allergies,
            loyaltyPoints = response.loyaltyPoints,
            loyaltyStatus = response.loyaltyStatus,
            ownEquipment = response.ownEquipment
        )
    }
}
