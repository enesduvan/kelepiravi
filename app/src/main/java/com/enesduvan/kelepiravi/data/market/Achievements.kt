package com.enesduvan.kelepiravi.data.market

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int, // Can be used later, for now we can use R.drawable.para or similar
    val rewardXp: Int,
    val rewardMoney: Double
)

object AchievementManager {
    val ALL_ACHIEVEMENTS = listOf(
        Achievement(
            id = "first_blood",
            title = "İlk Ticaret",
            description = "İlk eşyanı satın aldın. Hoş geldin!",
            iconRes = 0,
            rewardXp = 50,
            rewardMoney = 100.0
        ),
        Achievement(
            id = "first_sale",
            title = "İlk Kazanç",
            description = "İlk eşyanı sattın. Para akışı başladı!",
            iconRes = 0,
            rewardXp = 50,
            rewardMoney = 150.0
        ),
        Achievement(
            id = "ten_items_sold",
            title = "İşler Tıkırında",
            description = "Toplam 10 eşya sattın.",
            iconRes = 0,
            rewardXp = 200,
            rewardMoney = 500.0
        ),
        Achievement(
            id = "first_repair",
            title = "Tamirci Çırağı",
            description = "İlk eşyanı tamir ettin. Değer kattın!",
            iconRes = 0,
            rewardXp = 100,
            rewardMoney = 200.0
        ),
        Achievement(
            id = "one_week",
            title = "Bir Haftalık Emek",
            description = "Oyunda 7. güne ulaştın.",
            iconRes = 0,
            rewardXp = 300,
            rewardMoney = 1000.0
        )
    )

    fun checkAchievements(
        itemsBought: Int,
        itemsSold: Int,
        currentDay: Int,
        totalRepairs: Int, // if we track this later
        unlockedIds: List<String>
    ): List<Achievement> {
        val newlyUnlocked = mutableListOf<Achievement>()

        if (itemsBought >= 1 && !unlockedIds.contains("first_blood")) {
            newlyUnlocked.add(ALL_ACHIEVEMENTS.first { it.id == "first_blood" })
        }
        if (itemsSold >= 1 && !unlockedIds.contains("first_sale")) {
            newlyUnlocked.add(ALL_ACHIEVEMENTS.first { it.id == "first_sale" })
        }
        if (itemsSold >= 10 && !unlockedIds.contains("ten_items_sold")) {
            newlyUnlocked.add(ALL_ACHIEVEMENTS.first { it.id == "ten_items_sold" })
        }
        if (currentDay >= 7 && !unlockedIds.contains("one_week")) {
            newlyUnlocked.add(ALL_ACHIEVEMENTS.first { it.id == "one_week" })
        }
        // "first_repair" handled when a repair happens

        return newlyUnlocked
    }
}
