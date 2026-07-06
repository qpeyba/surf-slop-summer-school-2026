package com.qpeyba.surf_slop_summer_school_2026.data.remote.api

import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response.SlotPageResponse
import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response.SlotResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SlotsApi {
    @GET("api/v1/slots")
    suspend fun listSlots(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<SlotPageResponse>

    @GET("api/v1/slots/{slotId}")
    suspend fun getSlot(@Path("slotId") slotId: String): Response<SlotResponse>
}
