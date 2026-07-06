package com.qpeyba.surf_slop_summer_school_2026.domain.repository

import com.qpeyba.surf_slop_summer_school_2026.domain.model.Booking
import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingWithSlot
import com.qpeyba.surf_slop_summer_school_2026.domain.model.EquipmentType
import com.qpeyba.surf_slop_summer_school_2026.domain.model.PaginatedResult

interface BookingsRepository {
    suspend fun createBooking(slotId: String, equipmentType: EquipmentType, idempotencyKey: String): Result<Booking>
    suspend fun getBookings(expand: String?, limit: Int, offset: Int): Result<PaginatedResult<BookingWithSlot>>
    suspend fun getBooking(bookingId: String): Result<BookingWithSlot>
    suspend fun cancelBooking(bookingId: String): Result<Booking>
    suspend fun transferBooking(bookingId: String, newSlotId: String): Result<Booking>
    suspend fun upsertReview(bookingId: String, rating: Int, text: String?): Result<Unit>
}
