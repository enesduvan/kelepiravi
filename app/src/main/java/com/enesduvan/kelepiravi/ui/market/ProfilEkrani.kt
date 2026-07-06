package com.enesduvan.kelepiravi.ui.market

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.data.market.AchievementManager
import com.enesduvan.kelepiravi.ui.shared.formatBalance
import com.enesduvan.kelepiravi.ui.theme.*

@Composable
fun ProfilEkrani(viewModel: MarketViewModel) {
    val playerState by viewModel.playerState.collectAsState()

    val level = playerState.level
    val xp = playerState.xp
    val totalProfit = playerState.totalProfit
    val itemsBought = playerState.itemsBought
    val itemsSold = playerState.itemsSold

    val requiredXp = level * (level + 1) * GameConstants.XP_LEVEL_FACTOR
    val currentLevelBaseXp = (level - 1) * level * GameConstants.XP_LEVEL_FACTOR
    val targetProgress = (xp - currentLevelBaseXp).toFloat() / (requiredXp - currentLevelBaseXp).coerceAtLeast(1).toFloat()
    
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "xpProgress"
    )

    val badgeName = when {
        level < 5 -> "Acemi Çırak"
        level < 10 -> "İşportacı"
        level < 20 -> "Esnaf"
        level < 35 -> "Tüccar"
        else -> "Kelepir Avcısı"
    }
    
    val unlockedIds = if (playerState.unlockedAchievements.isEmpty()) emptyList() else playerState.unlockedAchievements.split(",")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text("Profilin", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Rozet ve Seviye Kartı
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(40.dp))
                            .background(PrimaryOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Lv\n$level", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(badgeName, color = PrimaryOrange, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // XP Progress Bar
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = MoneyGreen,
                        trackColor = SurfaceVariant,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$xp / $requiredXp XP", color = TextSecondary, fontSize = 14.sp)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            // İstatistikler
            Text("İstatistikler", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(modifier = Modifier.weight(1f), title = "Toplam Kâr", value = "₺${formatBalance(totalProfit.toString())}", color = MoneyGreen)
                StatCard(modifier = Modifier.weight(1f), title = "Oyun Günü", value = "Gün ${playerState.currentDay}", color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(modifier = Modifier.weight(1f), title = "Alınan Eşya", value = "$itemsBought", color = Color(0xFF64B5F6))
                StatCard(modifier = Modifier.weight(1f), title = "Satılan Eşya", value = "$itemsSold", color = Color(0xFFFF8A65))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Geliştirmeler", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Dükkan Geliştirmesi
            val shopCost = viewModel.getShopUpgradeCost(playerState.shopLevel)
            val maxShopCapacity = 5 + (playerState.shopLevel * 5)
            UpgradeCard(
                title = "Dükkan Kapasitesi (Lv ${playerState.shopLevel})",
                description = "Envanterine daha fazla eşya sığdır. Şu an: $maxShopCapacity",
                cost = shopCost,
                isMaxed = playerState.shopLevel >= 5,
                onUpgrade = { viewModel.upgradeShop() },
                playerBalance = playerState.balance.toDoubleOrNull() ?: 0.0
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Usta Geliştirmesi
            val mechanicCost = viewModel.getMechanicUpgradeCost(playerState.mechanicLevel)
            val mechanicFailure = ((0.40 - ((playerState.mechanicLevel - 1) * 0.10)).coerceAtLeast(0.0) * 100).toInt()
            UpgradeCard(
                title = "Usta Becerisi (Lv ${playerState.mechanicLevel})",
                description = "Tamir başarısızlık riskini azaltır. Şu anki risk: %$mechanicFailure",
                cost = mechanicCost,
                isMaxed = playerState.mechanicLevel >= 5,
                onUpgrade = { viewModel.upgradeMechanic() },
                playerBalance = playerState.balance.toDoubleOrNull() ?: 0.0
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Başarımlar", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(
            count = AchievementManager.ALL_ACHIEVEMENTS.size,
            key = { index -> AchievementManager.ALL_ACHIEVEMENTS[index].id }
        ) { index ->
            val ach = AchievementManager.ALL_ACHIEVEMENTS[index]
            val isUnlocked = unlockedIds.contains(ach.id)
            AchievementCard(ach = ach, isUnlocked = isUnlocked)
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AchievementCard(ach: com.enesduvan.kelepiravi.data.market.Achievement, isUnlocked: Boolean) {
    val bgColor = if (isUnlocked) Surface else SurfaceVariant
    val titleColor = if (isUnlocked) MoneyGreen else TextMuted
    val descColor = if (isUnlocked) TextPrimary else TextMuted
    val alphaValue = if (isUnlocked) 1f else 0.5f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 4.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isUnlocked) Color(0xFFE8F5E9) else Surface), // GREEN_BG is local to MarketScreen usually, using hex directly
                contentAlignment = Alignment.Center
            ) {
                Text(if (isUnlocked) "🏆" else "🔒", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f).alpha(alphaValue)) {
                Text(ach.title, color = titleColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(ach.description, color = descColor, fontSize = 12.sp)
                
                // Show rewards
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("+${ach.rewardXp} XP", color = MoneyGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("+₺${formatBalance(ach.rewardMoney.toString())}", color = MoneyGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun UpgradeCard(
    title: String,
    description: String,
    cost: Double,
    isMaxed: Boolean,
    onUpgrade: () -> Unit,
    playerBalance: Double
) {
    val canAfford = playerBalance >= cost

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = PrimaryOrange, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, color = TextSecondary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onUpgrade,
                enabled = !isMaxed && canAfford,
                colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isMaxed) {
                    Text("MAX", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Text("₺${formatBalance(cost.toString())}", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
