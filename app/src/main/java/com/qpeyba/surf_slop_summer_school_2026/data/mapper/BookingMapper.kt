package com.qpeyba.surf_slop_summer_school_2026.data.mapper

import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response.BookingDetailResponse
import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response.BookingResponse
import com.qpeyba.surf_slop_summer_school_2026.domain.model.Booking
import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingStatus
import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingWithSlot
import com.qpeyba.surf_slop_summer_school_2026.domain.model.EquipmentType
import com.qpeyba.surf_slop_summer_school_2026.util.DateFormatter
import javax.inject.Inject

class BookingMapper @Inject constructor(
    private val slotMapper: SlotMapper
) {
    fun toDomain(response: BookingResponse): Booking {
        return Booking(
            id = response.id,
            slotId = response.slotId,
            equipmentType = EquipmentType.fromApiValue(response.equipmentType),
            status = BookingStatus.fromApiValue(response.status),
            refundAmount = response.refundAmount,
            reviewRating = response.reviewRating,
            reviewText = response.reviewText,
            createdAt = DateFormatter.parseIsoDateTime(response.createdAt),
            cancelledAt = response.cancelledAt?.let { DateFormatter.parseIsoDateTime(it) }
        )
    }

    fun toDomainWithSlot(response: BookingDetailResponse): BookingWithSlot {
        return BookingWithSlot(
            booking = Booking(
                id = response.id,
                slotId = response.slotId,
                equipmentType = EquipmentType.fromApiValue(response.equipmentType),
                status = BookingStatus.fromApiValue(response.status),
                refundAmount = response.refundAmount,
                reviewRating = response.reviewRating,
                reviewText = response.reviewText,
                createdAt = DateFormatter.parseIsoDateTime(response.createdAt),
                cancelledAt = response.cancelledAt?.let { DateFormatter.parseIsoDateTime(it) }
            ),
            slot = slotMapper.toDomain(response.slot)
        )
    }
}
