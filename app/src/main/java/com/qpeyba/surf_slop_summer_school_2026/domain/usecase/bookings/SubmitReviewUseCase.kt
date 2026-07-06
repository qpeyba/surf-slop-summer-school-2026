package com.qpeyba.surf_slop_summer_school_2026.domain.usecase.bookings

import com.qpeyba.surf_slop_summer_school_2026.domain.repository.BookingsRepository
import javax.inject.Inject

class SubmitReviewUseCase @Inject constructor(
    private val bookingsRepository: BookingsRepository
) {
    suspend operator fun invoke(bookingId: String, rating: Int, text: String?): Result<Unit> {
        return bookingsRepository.upsertReview(bookingId, rating, text)
    }
}
