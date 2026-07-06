package com.qpeyba.surf_slop_summer_school_2026.data.remote.api

import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.request.UpdateProfileRequest
import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response.ProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface ProfileApi {
    @GET("api/v1/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @PATCH("api/v1/profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<ProfileResponse>
}
