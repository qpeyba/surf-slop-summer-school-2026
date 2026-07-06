package com.qpeyba.surf_slop_summer_school_2026.util

import java.util.UUID

object IdempotencyKey {
    fun generate(): String = UUID.randomUUID().toString()
}
