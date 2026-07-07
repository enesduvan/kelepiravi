package com.enesduvan.kelepiravi.data.event

import android.content.Context
import kotlinx.serialization.json.Json

object EventLoader {
    private var cachedEvents: List<EventDefinition>? = null

    fun loadEvents(context: Context): List<EventDefinition> {
        if (cachedEvents != null) return cachedEvents!!
        
        return try {
            val jsonString = context.assets.open("events.json").bufferedReader().use { it.readText() }
            val json = Json { ignoreUnknownKeys = true }
            cachedEvents = json.decodeFromString<List<EventDefinition>>(jsonString)
            cachedEvents!!
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
