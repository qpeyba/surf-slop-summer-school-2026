package com.qpeyba.surf_slop_summer_school_2026.domain.repository

import com.qpeyba.surf_slop_summer_school_2026.domain.model.User

interface ProfileRepository {
    suspend fun getProfile(): Result<User>
    suspend fun updateProfile(allergies: List<String>?, ownEquipment: Boolean?): Result<User>
}
