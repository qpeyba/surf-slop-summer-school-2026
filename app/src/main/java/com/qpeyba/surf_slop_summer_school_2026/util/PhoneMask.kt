package com.qpeyba.surf_slop_summer_school_2026.util

object PhoneMask {
    private const val MASK = "+7 (***) ***-**-**"

    fun apply(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        val normalized = if (digits.startsWith("7") || digits.startsWith("8")) {
            digits.drop(1)
        } else {
            digits
        }.take(10)

        val sb = StringBuilder()
        var digitIndex = 0
        for (ch in MASK) {
            if (ch == '*') {
                if (digitIndex < normalized.length) {
                    sb.append(normalized[digitIndex])
                    digitIndex++
                } else {
                    break
                }
            } else if (digitIndex < normalized.length || ch == '+') {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun rawDigits(masked: String): String {
        val digits = masked.filter { it.isDigit() }
        return if (digits.length >= 11) {
            digits.substring(1)
        } else {
            digits
        }
    }

    fun isValid(phone: String): Boolean {
        return phone.filter { it.isDigit() }.length == 11
    }

    fun toE164(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return "+7${digits.drop(1)}"
    }
}
