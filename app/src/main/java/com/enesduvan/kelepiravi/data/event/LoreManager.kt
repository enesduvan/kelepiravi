package com.enesduvan.kelepiravi.data.event

import com.enesduvan.kelepiravi.database.entity.UserInventoryEntity

object LoreManager {
    /**
     * Oyuncunun flag'lerine göre devam eden hikayelerini/söylentilerini döner.
     */
    fun getActiveLore(player: UserInventoryEntity): List<String> {
        val flags = player.eventFlags.split(",").filter { it.isNotEmpty() }.toSet()
        val loreList = mutableListOf<String>()
        
        if (flags.contains("HELPED_OLD_MAN")) {
            loreList.add("Mahalledeki yaşlı amcaya yardım ettin. Torunu sana minnettar kalabilir.")
        }
        
        if (flags.contains("HONEST_MERCHANT")) {
            loreList.add("Dürüst bir esnaf olarak tanınıyorsun. Müşteriler sana daha çok güveniyor.")
        }
        
        if (flags.contains("SCAMMED_CUSTOMER")) {
            loreList.add("Müşterilerden birini dolandırdığın dilden dile dolaşıyor. Dikkatli ol, zabıta gelebilir.")
        }

        if (flags.contains("PAID_DEBT_EARLY")) {
            loreList.add("Tefeci Rıza borcunu erken ödediğin için senden memnun.")
        }
        
        return loreList
    }
}
