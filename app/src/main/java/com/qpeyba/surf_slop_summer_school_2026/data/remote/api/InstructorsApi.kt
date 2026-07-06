package com.qpeyba.surf_slop_summer_school_2026.data.remote.api

import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response.InstructorResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface InstructorsApi {
    @GET("api/v1/instructors/{instructorId}")
    suspend fun getInstructor(@Path("instructorId") instructorId: String): Response<InstructorResponse>
}
