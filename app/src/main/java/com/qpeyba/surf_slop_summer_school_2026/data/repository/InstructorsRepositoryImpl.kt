package com.qpeyba.surf_slop_summer_school_2026.data.repository

import com.qpeyba.surf_slop_summer_school_2026.data.mapper.InstructorMapper
import com.qpeyba.surf_slop_summer_school_2026.data.remote.api.InstructorsApi
import com.qpeyba.surf_slop_summer_school_2026.domain.model.Instructor
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.InstructorsRepository
import javax.inject.Inject

class InstructorsRepositoryImpl @Inject constructor(
    private val instructorsApi: InstructorsApi,
    private val instructorMapper: InstructorMapper
) : InstructorsRepository {

    override suspend fun getInstructor(instructorId: String): Result<Instructor> {
        return try {
            val response = instructorsApi.getInstructor(instructorId)
            if (response.isSuccessful) {
                Result.success(instructorMapper.toDomain(response.body()!!))
            } else {
                Result.failure(Exception("get_instructor_failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
