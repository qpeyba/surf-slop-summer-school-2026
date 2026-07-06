package com.qpeyba.surf_slop_summer_school_2026.data.repository

import com.qpeyba.surf_slop_summer_school_2026.data.mapper.BookingMapper
import com.qpeyba.surf_slop_summer_school_2026.data.mapper.SlotMapper
import com.qpeyba.surf_slop_summer_school_2026.data.remote.api.BookingsApi
import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.request.CreateBookingRequest
import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.request.ReviewRequest
import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.request.TransferRequest
import com.qpeyba.surf_slop_summer_school_2026.domain.model.Booking
import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingWithSlot
import com.qpeyba.surf_slop_summer_school_2026.domain.model.EquipmentType
import com.qpeyba.surf_slop_summer_school_2026.domain.model.PaginatedResult
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.BookingsRepository
import javax.inject.Inject

class BookingsRepositoryImpl @Inject constructor(
    private val bookingsApi: BookingsApi,
    private val bookingMapper: BookingMapper,
    private val slotMapper: SlotMapper
) : BookingsRepository {

    override suspend fun createBooking(slotId: String, equipmentType: EquipmentType, idempotencyKey: String): Result<Booking> {
        return try {
            val request = CreateBookingRequest(slotId, equipmentType.apiValue)
            val response = bookingsApi.createBooking(request)
            if (response.isSuccessful) {
                Result.success(bookingMapper.toDomain(response.body()!!))
            } else {
                val errorCode = when (response.code()) {
                    409 -> "double_booking"
                    410 -> "slot_full"
                    422 -> "slot_cancelled"
                    423 -> "slot_started"
                    else -> "create_booking_failed: ${response.code()}"
                }
                Result.failure(Exception(errorCode))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBookings(expand: String?, limit: Int, offset: Int): Result<PaginatedResult<BookingWithSlot>> {
        return try {
            val response = bookingsApi.listBookings(expand = expand, limit = limit, offset = offset)
            if (response.isSuccessful) {
                val body = response.body()!!
                val items = body.items.mapNotNull { bookingResponse ->
                    val slot = bookingResponse.slot ?: return@mapNotNull null
                    BookingWithSlot(
                        booking = bookingMapper.toDomain(bookingResponse),
                        slot = slotMapper.toDomain(slot)
                    )
                }
                Result.success(PaginatedResult(items = items, total = body.total))
            } else {
                Result.failure(Exception("list_bookings_failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBooking(bookingId: String): Result<BookingWithSlot> {
        return try {
            val response = bookingsApi.getBooking(bookingId)
            if (response.isSuccessful) {
                Result.success(bookingMapper.toDomainWithSlot(response.body()!!))
            } else {
                Result.failure(Exception("get_booking_failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelBooking(bookingId: String): Result<Booking> {
        return try {
            val response = bookingsApi.cancelBooking(bookingId)
            if (response.isSuccessful) {
                Result.success(bookingMapper.toDomain(response.body()!!))
            } else {
                val errorCode = when (response.code()) {
                    409 -> "booking_not_active"
                    else -> "cancel_booking_failed: ${response.code()}"
                }
                Result.failure(Exception(errorCode))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun transferBooking(bookingId: String, newSlotId: String): Result<Booking> {
        return try {
            val response = bookingsApi.transferBooking(bookingId, TransferRequest(newSlotId))
            if (response.isSuccessful) {
                Result.success(bookingMapper.toDomain(response.body()!!.newBooking))
            } else {
                val errorCode = when {
                    response.code() == 410 -> "slot_full"
                    response.code() == 422 -> "slot_cancelled"
                    else -> "transfer_failed: ${response.code()}"
                }
                Result.failure(Exception(errorCode))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun upsertReview(bookingId: String, rating: Int, text: String?): Result<Unit> {
        return try {
            val response = bookingsApi.upsertReview(bookingId, ReviewRequest(rating, text))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorCode = when {
                    response.code() == 400 -> "invalid_rating"
                    response.code() == 403 -> "not_completed"
                    else -> "review_failed: ${response.code()}"
                }
                Result.failure(Exception(errorCode))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
