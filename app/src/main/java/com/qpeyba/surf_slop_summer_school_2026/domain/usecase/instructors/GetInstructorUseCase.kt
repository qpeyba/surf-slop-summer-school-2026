package com.qpeyba.surf_slop_summer_school_2026.domain.usecase.instructors

import com.qpeyba.surf_slop_summer_school_2026.domain.model.Instructor
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.InstructorsRepository
import javax.inject.Inject

class GetInstructorUseCase @Inject constructor(
    private val instructorsRepository: InstructorsRepository
) {
    suspend operator fun invoke(instructorId: String): Result<Instructor> {
        return instructorsRepository.getInstructor(instructorId)
    }
}
