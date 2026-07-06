package com.qpeyba.surf_slop_summer_school_2026.data.repository

import com.qpeyba.surf_slop_summer_school_2026.data.mapper.ProfileMapper
import com.qpeyba.surf_slop_summer_school_2026.data.remote.api.ProfileApi
import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.request.UpdateProfileRequest
import com.qpeyba.surf_slop_summer_school_2026.domain.model.User
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.ProfileRepository
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileApi: ProfileApi,
    private val profileMapper: ProfileMapper
) : ProfileRepository {

    override suspend fun getProfile(): Result<User> {
        return try {
            val response = profileApi.getProfile()
            if (response.isSuccessful) {
                Result.success(profileMapper.toDomain(response.body()!!))
            } else {
                Result.failure(Exception("get_profile_failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(allergies: List<String>?, ownEquipment: Boolean?): Result<User> {
        return try {
            val response = profileApi.updateProfile(UpdateProfileRequest(allergies, ownEquipment))
            if (response.isSuccessful) {
                Result.success(profileMapper.toDomain(response.body()!!))
            } else {
                Result.failure(Exception("update_profile_failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
