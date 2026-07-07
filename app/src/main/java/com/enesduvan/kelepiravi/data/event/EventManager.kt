package com.enesduvan.kelepiravi.data.event

import com.enesduvan.kelepiravi.data.local.entity.UserInventoryEntity
import kotlinx.serialization.json.Json

object EventManager {
    fun getAvailableEvents(
        player: UserInventoryEntity,
        allEvents: List<EventDefinition>
    ): List<EventDefinition> {
        val flags = player.eventFlags.split(",").filter { it.isNotEmpty() }.toSet()
        val cooldowns = runCatching {
            if (player.eventCooldowns.isBlank()) emptyMap<String, Int>()
            else Json.decodeFromString<Map<String, Int>>(player.eventCooldowns)
        }.getOrDefault(emptyMap())

        return allEvents.filter { event ->
            // Cooldown check
            val unlockDay = cooldowns[event.id]
            if (unlockDay != null && player.currentDay < unlockDay) return@filter false

            // Conditions check
            event.conditions.all { condition ->
                when (condition.type) {
                    "MIN_DAY" -> player.currentDay >= condition.value.toInt()
                    "MAX_DAY" -> player.currentDay <= condition.value.toInt()
                    "HAS_FLAG" -> flags.contains(condition.value)
                    "NOT_HAS_FLAG" -> !flags.contains(condition.value)
                    "MIN_MONEY" -> (player.balance.toDoubleOrNull() ?: 0.0) >= condition.value.toDouble()
                    "MIN_LEVEL" -> player.level >= condition.value.toInt()
                    else -> true
                }
            }
        }
    }

    fun pickRandomEvent(availableEvents: List<EventDefinition>): EventDefinition? {
        if (availableEvents.isEmpty()) return null
        val totalWeight = availableEvents.sumOf { it.weight }
        if (totalWeight <= 0) return null
        
        var randomVal = kotlin.random.Random.nextInt(totalWeight)
        for (event in availableEvents) {
            randomVal -= event.weight
            if (randomVal < 0) return event
        }
        return availableEvents.last()
    }
}
