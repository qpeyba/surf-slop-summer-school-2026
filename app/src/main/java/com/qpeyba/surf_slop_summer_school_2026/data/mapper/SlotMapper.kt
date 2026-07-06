package com.qpeyba.surf_slop_summer_school_2026.data.mapper

import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response.SlotResponse
import com.qpeyba.surf_slop_summer_school_2026.domain.model.Difficulty
import com.qpeyba.surf_slop_summer_school_2026.domain.model.Slot
import com.qpeyba.surf_slop_summer_school_2026.domain.model.SlotStatus
import com.qpeyba.surf_slop_summer_school_2026.util.DateFormatter
import javax.inject.Inject

class SlotMapper @Inject constructor(
    private val instructorMapper: InstructorMapper
) {
    fun toDomain(response: SlotResponse): Slot {
        return Slot(
            id = response.id,
            dateTime = DateFormatter.parseIsoDateTime(response.dateTime),
            menu = response.menu,
            photoUrls = response.photoUrls,
            difficulty = Difficulty.fromApiValue(response.difficulty),
            price = response.price,
            address = response.address,
            capacity = response.capacity,
            bookedCount = response.bookedCount,
            status = SlotStatus.fromApiValue(response.status),
            instructor = response.instructor?.let { instructorMapper.toDomain(it) }
        )
    }
}
