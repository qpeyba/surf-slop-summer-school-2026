package com.qpeyba.surf_slop_summer_school_2026.domain.usecase.bookings

import com.qpeyba.surf_slop_summer_school_2026.domain.model.Booking
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.BookingsRepository
import javax.inject.Inject

class TransferBookingUseCase @Inject constructor(
    private val bookingsRepository: BookingsRepository
) {
    suspend operator fun invoke(bookingId: String, newSlotId: String): Result<Booking> {
        return bookingsRepository.transferBooking(bookingId, newSlotId)
    }
}
