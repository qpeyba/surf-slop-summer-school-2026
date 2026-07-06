package com.qpeyba.surf_slop_summer_school_2026.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {
    private val isoDate = DateTimeFormatter.ISO_LOCAL_DATE
    private val isoDateTime = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val readableDate = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
    private val readableDateTime = DateTimeFormatter.ofPattern("d MMMM, HH:mm", Locale("ru"))
    private val readableTime = DateTimeFormatter.ofPattern("HH:mm", Locale("ru"))
    private val dayOfWeek = DateTimeFormatter.ofPattern("EEEE", Locale("ru"))
    private val dayOfWeekShort = DateTimeFormatter.ofPattern("EEE", Locale("ru"))

    fun toIsoString(date: LocalDate): String = date.format(isoDate)
    fun toIsoString(dateTime: LocalDateTime): String = dateTime.format(isoDateTime)
    fun parseIsoDate(value: String): LocalDate = LocalDate.parse(value, isoDate)
    fun parseIsoDateTime(value: String): LocalDateTime = OffsetDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME).toLocalDateTime()
    fun toReadableDate(date: LocalDate): String = date.format(readableDate)
    fun toReadableDateTime(dateTime: LocalDateTime): String = dateTime.format(readableDateTime)
    fun toReadableTime(dateTime: LocalDateTime): String = dateTime.format(readableTime)
    fun toDayOfWeek(date: LocalDate): String = date.format(dayOfWeek).replaceFirstChar { it.uppercase() }
    fun toDayOfWeekShort(date: LocalDate): String = date.format(dayOfWeekShort).replaceFirstChar { it.uppercase() }
}
