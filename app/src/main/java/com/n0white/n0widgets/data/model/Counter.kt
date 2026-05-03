package com.n0white.n0widgets.data.model

import android.content.Context
import com.n0white.n0widgets.R
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

    fun getRemainingTimeString(context: Context): String {
        return formatTime(context, LocalDate.now(), targetDate)
    }

    fun getPassedTimeString(context: Context): String {
        val days = ChronoUnit.DAYS.between(startDate, LocalDate.now()).coerceAtLeast(0).toInt()
        return context.resources.getQuantityString(R.plurals.days_count, days, days)
    }

    private fun formatTime(context: Context, from: LocalDate, to: LocalDate): String {
        if (from.isAfter(to)) return context.resources.getQuantityString(R.plurals.days_count, 0, 0)
        
        return when (formatMode) {
            CounterFormat.DAYS_ONLY -> {
                val days = ChronoUnit.DAYS.between(from, to).toInt()
                context.resources.getQuantityString(R.plurals.days_count, days, days)
            }
            CounterFormat.YMD -> {
                val period = Period.between(from, to)
                val parts = mutableListOf<String>()
                if (period.years > 0) parts.add(context.getString(R.string.years_short, period.years))
                if (period.months > 0) parts.add(context.getString(R.string.months_short, period.months))
                if (period.days > 0 || parts.isEmpty()) {
                    parts.add(context.getString(R.string.days_short, period.days))
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

    fun getMotivationPhrase(context: Context, isCompact: Boolean = false): String {
        val days = daysRemaining
        return when {
            days == 0L -> context.getString(if (isCompact) R.string.motivation_today_compact else R.string.motivation_today)
            days <= 3 -> context.getString(R.string.motivation_very_soon)
            days <= 7 -> context.getString(if (isCompact) R.string.motivation_soon_compact else R.string.motivation_soon)
            days <= 30 -> context.getString(if (isCompact) R.string.motivation_bit_more_compact else R.string.motivation_bit_more)
            else -> context.getString(R.string.motivation_keep_going)
        }
    }
}
