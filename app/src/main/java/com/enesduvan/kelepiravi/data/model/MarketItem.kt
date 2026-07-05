package com.enesduvan.kelepiravi.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MarketItem(
    val condition: String,
    val sellerName: String,
    val itemName: String,
    val salesValue: String,           // Satıcının istediği fiyat
    val estimatedValue: String,       // Tahmini gerçek değer (günlük değişir)
    val imageName: String,
    val category: String = "",        // Elektronik, Mobilya, Aksesuar vb.
    val purchasePrice: String = "",   // Envantere girerken set edilir
    val purchaseDate: String = "",    // ISO-8601: "2026-07-05"
    val dailyChangePercent: Double = 0.0  // Bugünkü % değişim (+ artış, - düşüş)
)
