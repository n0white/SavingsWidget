package com.n0white.savingswidget.data.model

import java.util.Calendar
import kotlin.math.roundToInt

data class Goal(
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val savedAmount: Double,
    val targetAmount: Double,
    val currency: String = "$",
    val isWavy: Boolean = true,
    val startOfMonthAmount: Double = 0.0,
    val lastUpdateMonth: Int = -1 // 0-11 for Calendar.MONTH
) {
    val progress: Float get() = if (targetAmount > 0) (savedAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val remaining: Double get() = (targetAmount - savedAmount).coerceAtLeast(0.0)
    
    val monthlyEfficiency: Int get() {
        if (savedAmount <= 0) return 0
        
        return if (startOfMonthAmount > 0) {
            val diff = savedAmount - startOfMonthAmount
            if (diff <= 0) 0 else ((diff / startOfMonthAmount) * 100).roundToInt()
        } else {
            // Fallback: If started with 0, show progress relative to target as efficiency
            ((savedAmount / targetAmount.coerceAtLeast(1.0)) * 100).roundToInt()
        }
    }
}
