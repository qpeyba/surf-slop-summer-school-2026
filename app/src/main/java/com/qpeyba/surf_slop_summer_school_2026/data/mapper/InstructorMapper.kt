package com.qpeyba.surf_slop_summer_school_2026.data.mapper

import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response.InstructorResponse
import com.qpeyba.surf_slop_summer_school_2026.domain.model.Instructor
import com.qpeyba.surf_slop_summer_school_2026.domain.model.InstructorStatus
import javax.inject.Inject

class InstructorMapper @Inject constructor() {
    fun toDomain(response: InstructorResponse): Instructor {
        return Instructor(
            id = response.id,
            name = response.name,
            status = InstructorStatus.fromApiValue(response.status),
            rating = response.rating,
            specialization = response.specialization
        )
    }
}
