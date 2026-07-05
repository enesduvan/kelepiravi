package com.enesduvan.kelepiravi.data.market

/**
 * Günlük piyasa olayı.
 * Belirli bir kategoriyi veya tüm piyasayı etkiler.
 */
data class DailyEvent(
    val emoji: String,
    val title: String,
    val description: String,
    val affectedCategory: String?,  // null = tüm kategoriler
    val effectPercent: Double        // Örn: +15.0 veya -10.0
) {
    val isPositive: Boolean get() = effectPercent >= 0
    val effectLabel: String get() {
        val sign = if (isPositive) "+" else ""
        return "$sign${effectPercent.toInt()}%"
    }
}
