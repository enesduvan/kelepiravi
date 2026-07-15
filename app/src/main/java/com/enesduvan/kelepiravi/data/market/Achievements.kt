package com.enesduvan.kelepiravi.data.market

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: ImageVector,
    val rewardXp: Int,
    val rewardMoney: Double,
    val isHidden: Boolean = false
)

object AchievementManager {
    val ALL_ACHIEVEMENTS = listOf(
        Achievement("first_blood", "Siftah Benden", "İlk eşyanı satın aldın. Ticarete hoş geldin!", Icons.Default.ShoppingCart, 50, 100.0),
        Achievement("first_sale", "Ticaretin Kanunu", "İlk eşyanı sattın. Para akışı başladı!", Icons.Default.AttachMoney, 50, 150.0),
        Achievement("ten_items_sold", "İşler Tıkırında", "Toplam 10 eşya sattın.", Icons.Default.TrendingUp, 200, 500.0),
        Achievement("first_repair", "Tamirci Çırağı", "İlk eşyanı tamir ettin. Değer kattın!", Icons.Default.Build, 100, 200.0),
        Achievement("repair_master", "Usta Eller", "Toplam 5 eşya tamir ettin.", Icons.Default.Handyman, 300, 1000.0),
        Achievement("millionaire", "Milyoner!", "Bakiye 1.000.000₺'ye ulaştı.", Icons.Default.WorkspacePremium, 1000, 0.0),
        Achievement("scammed", "Soğuk Su İç", "Kazıklanarak sahte bir ürün satın aldın.", Icons.Default.Warning, 50, 50.0, isHidden = true),
        Achievement("alien_tech", "Absürt Koleksiyoncu", "NASA Bilgisayarı veya F-16 satın aldın.", Icons.Default.RocketLaunch, 500, 5000.0, isHidden = true)
    )

    fun checkAchievements(
        balance: Double,
        itemsBought: Int,
        itemsSold: Int,
        totalRepairs: Int,
        boughtScam: Boolean,
        boughtAbsurd: Boolean,
        unlockedIds: List<String>
    ): List<Achievement> {
        val newlyUnlocked = mutableListOf<Achievement>()

        fun unlock(id: String) {
            if (!unlockedIds.contains(id)) {
                newlyUnlocked.add(ALL_ACHIEVEMENTS.first { it.id == id })
            }
        }

        if (itemsBought >= 1) unlock("first_blood")
        if (itemsSold >= 1) unlock("first_sale")
        if (itemsSold >= 10) unlock("ten_items_sold")
        if (totalRepairs >= 1) unlock("first_repair")
        if (totalRepairs >= 5) unlock("repair_master")
        if (balance >= 1000000.0) unlock("millionaire")
        if (boughtScam) unlock("scammed")
        if (boughtAbsurd) unlock("alien_tech")

        return newlyUnlocked
    }
}
