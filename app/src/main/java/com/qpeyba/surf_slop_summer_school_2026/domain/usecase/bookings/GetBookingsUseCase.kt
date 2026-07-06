package com.qpeyba.surf_slop_summer_school_2026.domain.usecase.bookings

import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingWithSlot
import com.qpeyba.surf_slop_summer_school_2026.domain.model.PaginatedResult
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.BookingsRepository
import javax.inject.Inject

class GetBookingsUseCase @Inject constructor(
    private val bookingsRepository: BookingsRepository
) {
    suspend operator fun invoke(
        expand: String? = "slot",
        limit: Int = 20,
        offset: Int = 0
    ): Result<PaginatedResult<BookingWithSlot>> {
        return bookingsRepository.getBookings(expand, limit, offset)
    }
}
