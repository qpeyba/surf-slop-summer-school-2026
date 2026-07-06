package com.qpeyba.surf_slop_summer_school_2026.data.remote.api

import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.request.CreateBookingRequest
import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.request.ReviewRequest
import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.request.TransferRequest
import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response.BookingDetailResponse
import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response.BookingPageResponse
import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response.BookingResponse
import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response.ReviewResponse
import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response.TransferResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BookingsApi {
    @POST("api/v1/bookings")
    suspend fun createBooking(@Body body: CreateBookingRequest): Response<BookingResponse>

    @GET("api/v1/bookings")
    suspend fun listBookings(
        @Query("expand") expand: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<BookingPageResponse>

    @GET("api/v1/bookings/{bookingId}")
    suspend fun getBooking(@Path("bookingId") bookingId: String): Response<BookingDetailResponse>

    @POST("api/v1/bookings/{bookingId}/cancel")
    suspend fun cancelBooking(@Path("bookingId") bookingId: String): Response<BookingResponse>

    @POST("api/v1/bookings/{bookingId}/transfer")
    suspend fun transferBooking(
        @Path("bookingId") bookingId: String,
        @Body body: TransferRequest
    ): Response<TransferResponse>

    @PUT("api/v1/bookings/{bookingId}/review")
    suspend fun upsertReview(
        @Path("bookingId") bookingId: String,
        @Body body: ReviewRequest
    ): Response<ReviewResponse>
}
