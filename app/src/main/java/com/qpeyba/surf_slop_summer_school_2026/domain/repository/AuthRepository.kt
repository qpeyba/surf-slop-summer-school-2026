package com.qpeyba.surf_slop_summer_school_2026.domain.repository

interface AuthRepository {
    suspend fun requestOtp(phone: String): Result<Unit>
    suspend fun verifyOtp(phone: String, code: String): Result<String>
    suspend fun hasValidToken(): Boolean
    suspend fun logout()
    suspend fun saveToken(token: String, expiresIn: Long)
}
