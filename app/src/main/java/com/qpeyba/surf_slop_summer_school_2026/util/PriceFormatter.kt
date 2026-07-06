package com.qpeyba.surf_slop_summer_school_2026.util

object PriceFormatter {
    fun format(amount: Long): String {
        val s = amount.toString()
        val sb = StringBuilder()
        var count = 0
        for (i in s.lastIndex downTo 0) {
            if (count > 0 && count % 3 == 0) sb.insert(0, ' ')
            sb.insert(0, s[i])
            count++
        }
        return "${sb} ₽"
    }
}
