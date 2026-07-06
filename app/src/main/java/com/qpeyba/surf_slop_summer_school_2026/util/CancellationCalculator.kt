package com.qpeyba.surf_slop_summer_school_2026.util

import com.qpeyba.surf_slop_summer_school_2026.domain.model.CancellationInfo
import com.qpeyba.surf_slop_summer_school_2026.domain.model.CancellationVariant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object CancellationCalculator {
    fun calculate(slotDateTime: LocalDateTime, price: Long): CancellationInfo {
        val now = LocalDateTime.now()
        val hoursUntilStart = ChronoUnit.MINUTES.between(now, slotDateTime) / 60.0

        if (hoursUntilStart <= 0) {
            return CancellationInfo(
                canCancel = false,
                hoursRemaining = 0.0,
                refundPercentage = null,
                refundAmount = null,
                seatsReturned = false,
                warningText = "Невозможно отменить: класс уже начался.",
                variant = CancellationVariant.STARTED
            )
        }

        return if (hoursUntilStart > Constants.CANCELLATION_THRESHOLD_HOURS) {
            CancellationInfo(
                canCancel = true,
                hoursRemaining = hoursUntilStart,
                refundPercentage = 100,
                refundAmount = price,
                seatsReturned = true,
                warningText = "Бесплатная отмена. Место вернётся в расписание.",
                variant = CancellationVariant.A
            )
        } else {
            val refundAmount = (price * 50) / 100
            CancellationInfo(
                canCancel = true,
                hoursRemaining = hoursUntilStart,
                refundPercentage = 50,
                refundAmount = refundAmount,
                seatsReturned = true,
                warningText = "50% возврат: ${PriceFormatter.format(refundAmount)}. Возврат осуществляется владельцем студии вручную.",
                variant = CancellationVariant.B
            )
        }
    }
}
