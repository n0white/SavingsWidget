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
    val isInfinite: Boolean = false,
    val isWavy: Boolean = true,
    val backgroundImagePath: String? = null,
    val isBlurEnabled: Boolean = false,
    val customPrimary: Int? = null,
    val customPrimaryInverse: Int? = null,
    val customOnSurface: Int? = null,
    val customOnSurfaceInverse: Int? = null,
    val customSecondaryContainer: Int? = null
) {
    val totalDays: Long get() = ChronoUnit.DAYS.between(startDate, targetDate).coerceAtLeast(1)
    val daysPassed: Long get() = ChronoUnit.DAYS.between(startDate, LocalDate.now()).coerceAtLeast(0)
    val daysRemaining: Long get() = ChronoUnit.DAYS.between(LocalDate.now(), targetDate).coerceAtLeast(0)

    val progress: Float get() = if (isInfinite) {
        val milestone = getNextMilestoneDate()
        val total = ChronoUnit.DAYS.between(startDate, milestone).coerceAtLeast(1)
        val passed = ChronoUnit.DAYS.between(startDate, LocalDate.now()).coerceAtLeast(0)
        (passed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    } else {
        (daysPassed.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
    }

    fun getRemainingTimeString(context: Context): String {
        return if (isInfinite) {
            val milestoneDate = getNextMilestoneDate()
            val days = ChronoUnit.DAYS.between(LocalDate.now(), milestoneDate).coerceAtLeast(0).toInt()
            context.resources.getQuantityString(R.plurals.days_count, days, days)
        } else {
            formatTime(context, LocalDate.now(), targetDate)
        }
    }

    fun getPassedTimeString(context: Context): String {
        val daysPassed = ChronoUnit.DAYS.between(startDate, LocalDate.now()).coerceAtLeast(0).toInt()
        val timeStr = when (formatMode) {
            CounterFormat.DAYS_ONLY -> {
                context.resources.getQuantityString(R.plurals.days_count, daysPassed, daysPassed)
            }
            CounterFormat.YMD -> {
                formatTime(context, startDate, LocalDate.now())
            }
        }
        return if (isInfinite) context.getString(R.string.infinite_for_prefix, timeStr) else timeStr
    }

    fun getNextMilestoneDate(): LocalDate {
        val weeks = listOf(1, 2, 3, 4)
        for (w in weeks) {
            val date = startDate.plusWeeks(w.toLong())
            if (date.isAfter(LocalDate.now())) return date
        }

        val months = listOf(1, 2, 3, 4, 5, 6)
        for (m in months) {
            val date = startDate.plusMonths(m.toLong())
            if (date.isAfter(LocalDate.now())) return date
        }

        var years = 1
        while (true) {
            val date = startDate.plusYears(years.toLong())
            if (date.isAfter(LocalDate.now())) return date
            years++
        }
    }

    fun getNextMilestoneString(context: Context): String {
        val now = LocalDate.now()
        
        // Weeks
        for (w in 1..4) {
            val date = startDate.plusWeeks(w.toLong())
            if (date.isAfter(now)) return context.resources.getQuantityString(R.plurals.weeks_count, w, w)
        }

        // Months 1-6
        for (m in 1..6) {
            val date = startDate.plusMonths(m.toLong())
            if (date.isAfter(now)) return context.resources.getQuantityString(R.plurals.months_count, m, m)
        }

        // Years
        var y = 1
        while (true) {
            val date = startDate.plusYears(y.toLong())
            if (date.isAfter(now)) return context.resources.getQuantityString(R.plurals.years_count, y, y)
            y++
        }
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
                if (period.years > 0) {
                    val language = context.resources.configuration.locales[0].language
                    val yearStr = if (language == "ru") {
                        val lastDigit = period.years % 10
                        val lastTwoDigits = period.years % 100
                        if (lastTwoDigits in 11..19 || lastDigit in 5..9 || lastDigit == 0) {
                            context.getString(R.string.years_short_other, period.years)
                        } else {
                            context.getString(R.string.years_short_one, period.years)
                        }
                    } else {
                        context.getString(R.string.years_short, period.years)
                    }
                    parts.add(yearStr)
                }
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
        return targetDate.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }

    fun getMotivationPhrase(context: Context, isCompact: Boolean = false): String {
        if (isInfinite) {
            return if (isCompact) {
                context.getString(R.string.motivation_infinite_success)
            } else {
                context.getString(R.string.motivation_keep_going)
            }
        }
        val days = daysRemaining
        return when {
            days == 0L -> context.getString(if (isCompact) R.string.motivation_today_compact else R.string.motivation_today)
            days <= 3 -> context.getString(R.string.motivation_very_soon)
            days <= 7 -> context.getString(if (isCompact) R.string.motivation_soon_compact else R.string.motivation_soon)
            days <= 30 -> context.getString(if (isCompact) R.string.motivation_bit_more_compact else R.string.motivation_bit_more)
            else -> context.getString(if (isCompact) R.string.motivation_keep_going_compact else R.string.motivation_keep_going)
        }
    }
}
