package com.enesduvan.kelepiravi.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Listing(
    val id: String = UUID.randomUUID().toString(),
    val item: MarketItem,
    val listedPrice: String,
    val listedDay: Int,
    val views: Int = 0,
    val favorites: Int = 0,
    val offers: List<Offer> = emptyList(),
    val isSold: Boolean = false
)
