package com.qpeyba.surf_slop_summer_school_2026.domain.usecase.slots

import com.qpeyba.surf_slop_summer_school_2026.domain.model.Slot
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.SlotsRepository
import javax.inject.Inject

class GetSlotDetailUseCase @Inject constructor(
    private val slotsRepository: SlotsRepository
) {
    suspend operator fun invoke(slotId: String): Result<Slot> {
        return slotsRepository.getSlot(slotId)
    }
}
