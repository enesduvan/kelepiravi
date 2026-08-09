package com.enesduvan.kelepiravi.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Offer(
    val id: String = UUID.randomUUID().toString(),
    val npcName: String,
    val offerAmount: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val status: OfferStatus = OfferStatus.PENDING
)

enum class OfferStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
