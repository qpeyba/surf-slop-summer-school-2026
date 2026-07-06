package com.qpeyba.surf_slop_summer_school_2026.domain.usecase.profile

import com.qpeyba.surf_slop_summer_school_2026.domain.model.User
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(allergies: List<String>?, ownEquipment: Boolean?): Result<User> {
        return profileRepository.updateProfile(allergies, ownEquipment)
    }
}
