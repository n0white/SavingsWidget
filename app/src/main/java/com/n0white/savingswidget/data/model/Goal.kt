package com.n0white.savingswidget.data.model

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
    val lastUpdateMonth: Int = -1, // 0-11 for Calendar.MONTH
    val backgroundImagePath: String? = null,
    val isBlurEnabled: Boolean = false,
    val customPrimary: Int? = null,
    val customOnSurface: Int? = null,
    val customSecondaryContainer: Int? = null
) {
    val progress: Float get() = if (targetAmount > 0) (savedAmount / targetAmount).toFloat().coerceAtLeast(0f) else 0f
    val remaining: Double get() = (targetAmount - savedAmount).coerceAtLeast(0.0)
    
    val monthlyEfficiency: Int get() {
        if (savedAmount == startOfMonthAmount) return 0
        
        if (startOfMonthAmount <= 0.0) {
            if (savedAmount == 0.0) return 0
            // Fallback: If started with 0, show progress relative to target as efficiency
            return ((savedAmount / targetAmount.coerceAtLeast(1.0)) * 100).roundToInt()
        }
        
        val diff = savedAmount - startOfMonthAmount
        return ((diff / startOfMonthAmount) * 100).roundToInt()
    }
}
