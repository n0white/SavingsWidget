package com.n0white.n0widgets.data.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class CounterFormat {
    DAYS_ONLY,
    YMD // Years, Months, Days
}

data class Counter(
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val startDate: LocalDate,
    val targetDate: LocalDate,
    val formatMode: CounterFormat = CounterFormat.DAYS_ONLY,
    val isWavy: Boolean = true,
    val backgroundImagePath: String? = null,
    val isBlurEnabled: Boolean = false,
    val customPrimary: Int? = null,
    val customOnSurface: Int? = null,
    val customSecondaryContainer: Int? = null
) {
    val totalDays: Long get() = ChronoUnit.DAYS.between(startDate, targetDate).coerceAtLeast(1)
    val daysPassed: Long get() = ChronoUnit.DAYS.between(startDate, LocalDate.now()).coerceAtLeast(0)
    val daysRemaining: Long get() = ChronoUnit.DAYS.between(LocalDate.now(), targetDate).coerceAtLeast(0)

    val progress: Float get() = (daysPassed.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)

    fun getRemainingTimeString(): String {
        return formatTime(LocalDate.now(), targetDate)
    }

    fun getPassedTimeString(): String {
        val days = ChronoUnit.DAYS.between(startDate, LocalDate.now()).coerceAtLeast(0)
        return if (days == 1L) "1 day" else "$days days"
    }

    private fun formatTime(from: LocalDate, to: LocalDate): String {
        if (from.isAfter(to)) return "0 days"
        
        return when (formatMode) {
            CounterFormat.DAYS_ONLY -> {
                val days = ChronoUnit.DAYS.between(from, to)
                if (days == 1L) "1 day" else "$days days"
            }
            CounterFormat.YMD -> {
                val period = Period.between(from, to)
                val parts = mutableListOf<String>()
                if (period.years > 0) parts.add("${period.years} y")
                if (period.months > 0) parts.add("${period.months} m")
                if (period.days > 0 || parts.isEmpty()) {
                    parts.add("${period.days} d")
                }
                parts.joinToString(", ")
            }
        }
    }

    fun getTargetDateString(isShort: Boolean = false): String {
        val currentYear = LocalDate.now().year
        val pattern = if (isShort) {
            if (targetDate.year == currentYear) "d MMM" else "d MMM yy"
        } else {
            if (targetDate.year == currentYear) "d MMMM" else "d MMMM yyyy"
        }
        return targetDate.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH))
    }

    fun getMotivationPhrase(isCompact: Boolean = false): String {
        val days = daysRemaining
        return when {
            days == 0L -> if (isCompact) "Today!" else "Today is the day!"
            days <= 3 -> if (isCompact) "Very soon" else "Very soon"
            days <= 7 -> if (isCompact) "Soon" else "Coming soon"
            days <= 30 -> if (isCompact) "Bit more" else "Just a little more"
            else -> if (isCompact) "Keep going" else "Keep going"
        }
    }
}
