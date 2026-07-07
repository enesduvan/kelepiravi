package com.enesduvan.kelepiravi.data.npc

import com.enesduvan.kelepiravi.data.local.entity.UserInventoryEntity

data class Npc(
    val id: String,
    val name: String,
    val role: String,
    val defaultDialogs: List<String>
)

object NpcManager {
    val neighborhoodNpcs = listOf(
        Npc(
            id = "NPC_HAYRI",
            name = "Hayri Usta",
            role = "Tamirci",
            defaultDialogs = listOf(
                "Bugün işler kesat yeğenim.",
                "Getir bakalım şu aleti, bir el atalım.",
                "Eskisi gibi sağlam mal yapmıyorlar artık."
            )
        ),
        Npc(
            id = "NPC_RIZA",
            name = "Tefeci Rıza",
            role = "Borç Veren",
            defaultDialogs = listOf(
                "Nakit lazımsa buradayız.",
                "Faizler yüksek bu ara, haberin olsun.",
                "Borcunu gününde öde, canımı sıkma."
            )
        ),
        Npc(
            id = "NPC_SEVIM",
            name = "Sevim Teyze",
            role = "Mahalleli",
            defaultDialogs = listOf(
                "Hayırlı işler evladım.",
                "Geçen sattığın radyo çok güzel çıktı sağ ol.",
                "Aman dikkat et, buralarda dolandırıcı çok."
            )
        )
    )

    fun getAffinity(player: UserInventoryEntity, npcId: String): Int {
        val flags = player.eventFlags.split(",").filter { it.isNotEmpty() }
        var affinity = 0
        
        when (npcId) {
            "NPC_HAYRI" -> {
                if (flags.contains("HELPED_HAYRI")) affinity += 20
                if (flags.contains("CHEATED_HAYRI")) affinity -= 30
            }
            "NPC_SEVIM" -> {
                if (flags.contains("HONEST_MERCHANT")) affinity += 15
                if (flags.contains("HELPED_OLD_MAN")) affinity += 10
            }
            "NPC_RIZA" -> {
                if (flags.contains("PAID_DEBT_EARLY")) affinity += 10
            }
        }
        
        return affinity
    }

    fun getGreeting(player: UserInventoryEntity, npcId: String): String {
        val npc = neighborhoodNpcs.find { it.id == npcId } ?: return "Merhaba."
        val affinity = getAffinity(player, npcId)
        
        return when {
            affinity > 15 -> "Ooo kardeşim hoş geldin! Nasılsın?"
            affinity < -10 -> "Yine mi sen... Ne istiyorsun?"
            else -> npc.defaultDialogs.random()
        }
    }
}
