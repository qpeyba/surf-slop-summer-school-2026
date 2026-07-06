package com.qpeyba.surf_slop_summer_school_2026.domain.usecase.bookings

import com.qpeyba.surf_slop_summer_school_2026.domain.model.Booking
import com.qpeyba.surf_slop_summer_school_2026.domain.model.EquipmentType
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.BookingsRepository
import javax.inject.Inject

class CreateBookingUseCase @Inject constructor(
    private val bookingsRepository: BookingsRepository
) {
    suspend operator fun invoke(
        slotId: String,
        equipmentType: EquipmentType,
        idempotencyKey: String
    ): Result<Booking> {
        return bookingsRepository.createBooking(slotId, equipmentType, idempotencyKey)
    }
}
