package com.qpeyba.surf_slop_summer_school_2026.domain.repository

import com.qpeyba.surf_slop_summer_school_2026.domain.model.Instructor

interface InstructorsRepository {
    suspend fun getInstructor(instructorId: String): Result<Instructor>
}
