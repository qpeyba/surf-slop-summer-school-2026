package com.qpeyba.surf_slop_summer_school_2026.domain.usecase.bookings

import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingWithSlot
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.BookingsRepository
import javax.inject.Inject

class GetBookingDetailUseCase @Inject constructor(
    private val bookingsRepository: BookingsRepository
) {
    suspend operator fun invoke(bookingId: String): Result<BookingWithSlot> {
        return bookingsRepository.getBooking(bookingId)
    }
}
