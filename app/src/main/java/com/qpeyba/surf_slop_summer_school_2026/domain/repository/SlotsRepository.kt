package com.qpeyba.surf_slop_summer_school_2026.domain.repository

import com.qpeyba.surf_slop_summer_school_2026.domain.model.PaginatedResult
import com.qpeyba.surf_slop_summer_school_2026.domain.model.Slot
import java.time.LocalDate

interface SlotsRepository {
    suspend fun getSlots(from: LocalDate, to: LocalDate, limit: Int, offset: Int): Result<PaginatedResult<Slot>>
    suspend fun getSlot(slotId: String): Result<Slot>
}
