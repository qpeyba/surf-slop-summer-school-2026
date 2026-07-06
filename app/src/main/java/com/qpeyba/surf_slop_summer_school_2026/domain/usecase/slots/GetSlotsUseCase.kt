package com.qpeyba.surf_slop_summer_school_2026.domain.usecase.slots

import com.qpeyba.surf_slop_summer_school_2026.domain.model.PaginatedResult
import com.qpeyba.surf_slop_summer_school_2026.domain.model.Slot
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.SlotsRepository
import java.time.LocalDate
import javax.inject.Inject

class GetSlotsUseCase @Inject constructor(
    private val slotsRepository: SlotsRepository
) {
    suspend operator fun invoke(
        from: LocalDate,
        to: LocalDate,
        limit: Int = 20,
        offset: Int = 0
    ): Result<PaginatedResult<Slot>> {
        return slotsRepository.getSlots(from, to, limit, offset)
    }
}
