package com.example.savingswidget.data.model

data class Goal(
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val savedAmount: Double,
    val targetAmount: Double,
    val currency: String = "$",
    val isWavy: Boolean = true
) {
    val progress: Float get() = if (targetAmount > 0) (savedAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val remaining: Double get() = (targetAmount - savedAmount).coerceAtLeast(0.0)
}