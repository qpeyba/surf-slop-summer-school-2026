package com.qpeyba.surf_slop_summer_school_2026.data.repository

import com.qpeyba.surf_slop_summer_school_2026.data.mapper.SlotMapper
import com.qpeyba.surf_slop_summer_school_2026.data.remote.api.SlotsApi
import com.qpeyba.surf_slop_summer_school_2026.domain.model.PaginatedResult
import com.qpeyba.surf_slop_summer_school_2026.domain.model.Slot
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.SlotsRepository
import com.qpeyba.surf_slop_summer_school_2026.util.DateFormatter
import java.time.LocalDate
import javax.inject.Inject

class SlotsRepositoryImpl @Inject constructor(
    private val slotsApi: SlotsApi,
    private val slotMapper: SlotMapper
) : SlotsRepository {

    override suspend fun getSlots(from: LocalDate, to: LocalDate, limit: Int, offset: Int): Result<PaginatedResult<Slot>> {
        return try {
            val response = slotsApi.listSlots(
                from = DateFormatter.toIsoString(from),
                to = DateFormatter.toIsoString(to),
                limit = limit,
                offset = offset
            )
            if (response.isSuccessful) {
                val body = response.body()!!
                Result.success(
                    PaginatedResult(
                        items = body.items.map(slotMapper::toDomain),
                        total = body.total
                    )
                )
            } else {
                Result.failure(Exception("list_slots_failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSlot(slotId: String): Result<Slot> {
        return try {
            val response = slotsApi.getSlot(slotId)
            if (response.isSuccessful) {
                Result.success(slotMapper.toDomain(response.body()!!))
            } else {
                Result.failure(Exception("get_slot_failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
