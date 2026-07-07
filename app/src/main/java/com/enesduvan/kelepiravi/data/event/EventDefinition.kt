package com.enesduvan.kelepiravi.data.event

import kotlinx.serialization.Serializable

@Serializable
data class EventDefinition(
    val id: String,
    val title: String,
    val description: String,
    val npc: String? = null,
    val weight: Int = 100, // Çıkma olasılığı ağırlığı
    val cooldown: Int = 0, // Gün cinsinden tekrar çıkma süresi
    val conditions: List<EventCondition> = emptyList(), // Eventin çıkması için gereken şartlar
    val choices: List<EventChoice> = emptyList(), // Oyuncuya sunulan seçenekler (Boşsa direkt ödül/ceza uygulanır)
    val rewards: List<EventReward> = emptyList(), // Direk uygulanan ödüller
    val penalties: List<EventPenalty> = emptyList(), // Direk uygulanan cezalar
    val flags: List<String> = emptyList(), // Event tetiklendiğinde kaydedilecek flagler (Lore için)
    val followUp: String? = null // Bu event bittikten sonra tetiklenecek zorunlu veya zincirleme event ID'si
)

@Serializable
data class EventCondition(
    val type: String, // Örn: "MIN_DAY", "HAS_FLAG", "NOT_HAS_FLAG", "MIN_MONEY", "MIN_LEVEL"
    val value: String
)

@Serializable
data class EventChoice(
    val id: String,
    val text: String,
    val rewards: List<EventReward> = emptyList(),
    val penalties: List<EventPenalty> = emptyList(),
    val flags: List<String> = emptyList(),
    val followUp: String? = null
)

@Serializable
data class EventReward(
    val type: String, // Örn: "MONEY", "XP", "ITEM", "UNLOCK_ACHIEVEMENT"
    val value: String
)

@Serializable
data class EventPenalty(
    val type: String, // Örn: "MONEY_PERCENT", "MONEY_EXACT", "ITEM"
    val value: String
)
