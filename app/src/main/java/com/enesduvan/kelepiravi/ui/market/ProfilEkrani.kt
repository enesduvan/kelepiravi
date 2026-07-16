package com.enesduvan.kelepiravi.ui.market

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.enesduvan.kelepiravi.ui.listing.ListingViewModel
import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.data.market.AchievementManager
import com.enesduvan.kelepiravi.ui.shared.formatBalance
import com.enesduvan.kelepiravi.ui.theme.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ProfilEkrani(viewModel: MarketViewModel, listingViewModel: ListingViewModel) {
    var showSettings by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val isSoundEnabled by listingViewModel.isSoundEnabled.collectAsState()
    val isHapticEnabled by listingViewModel.isHapticEnabled.collectAsState()
    val isQuickSellEnabled by listingViewModel.isFastSellEnabled.collectAsState()

    val playerState by viewModel.playerState.collectAsState()

    val level = playerState.level
    val xp = playerState.xp
    val totalProfit = playerState.totalProfit
    val itemsBought = playerState.itemsBought
    val itemsSold = playerState.itemsSold
    
    // V6.0: Yeni İstatistikler
    val highestProfit = playerState.highestProfit
    val successRate = if (playerState.totalBargains > 0) {
        (playerState.successfulBargains.toFloat() / playerState.totalBargains.toFloat() * 100).toInt()
    } else 0
    val bestCategory = playerState.soldCategories.maxByOrNull { it.value }?.key ?: "Yok"
    
    val requiredXp = level * (level + 1) * GameConstants.XP_LEVEL_FACTOR
    val currentLevelBaseXp = (level - 1) * level * GameConstants.XP_LEVEL_FACTOR
    val targetProgress = (xp - currentLevelBaseXp).toFloat() / (requiredXp - currentLevelBaseXp).coerceAtLeast(1).toFloat()
    
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "xpProgress"
    )

    val badgeName = when {
        level < 5 -> "Sokak Satıcısı"
        level < 10 -> "Mahalle Esnafı"
        level < 15 -> "Profesyonel Flipper"
        level < 20 -> "Tüccar"
        else -> "Flipping Master"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MarketplaceBackground)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(32.dp))
            
            // ÜST BAR: Bakiye, İtibar, Seviye
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bakiye
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(CardSecondary, RoundedCornerShape(12.dp))
                        .border(1.dp, MarketBorderSoft, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AttachMoney, contentDescription = "Para", tint = BalanceGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("₺${formatBalance(playerState.balance)}", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Bakiye", color = MarketTextSecondary, fontSize = 10.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                // İtibar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(CardSecondary, RoundedCornerShape(12.dp))
                        .border(1.dp, MarketBorderSoft, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "İtibar", tint = ReputationGold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("${playerState.xp / 10}", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("İtibar", color = MarketTextSecondary, fontSize = 10.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Ayarlar (Seviye yerine Ayarlar ikonunu buraya koyalım)
                IconButton(
                    onClick = { showSettings = true },
                    modifier = Modifier
                        .size(56.dp)
                        .background(CardSecondary, RoundedCornerShape(12.dp))
                        .border(1.dp, MarketBorderSoft, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Ayarlar", tint = MarketTextSecondary)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // KULLANICI PROFİL KARTI
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Card, RoundedCornerShape(20.dp))
                    .border(1.dp, MarketBorderSoft, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar Ring
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(CardSecondary, RoundedCornerShape(36.dp))
                            .border(2.dp, PrimaryOrange, RoundedCornerShape(36.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Avatar", tint = PrimaryOrange, modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(badgeName, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Profesyonel Flipper", color = MarketTextSecondary, fontSize = 14.sp)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Seviye $level", color = PrimaryOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("$xp / $requiredXp XP", color = MarketTextSecondary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = PrimaryOrange,
                            trackColor = SurfaceVariant,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // İSTATİSTİKLER BÖLÜMÜ
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("İSTATİSTİKLER", color = MarketTextSecondary, fontSize = 12.sp, letterSpacing = 1.sp)
                Text("Tüm Zamanlar ▾", color = MarketTextSecondary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Toplam Kâr
                StatBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.TrendingUp,
                    iconTint = MoneyGreen,
                    title = "Toplam Kâr",
                    value = "₺${formatBalance(totalProfit.toString())}",
                    subValue = "Net Kâr",
                    chartColor = MoneyGreen
                )
                // En Büyük Kâr
                StatBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Star,
                    iconTint = Color(0xFFFFD700),
                    title = "En Büyük Kâr",
                    value = "₺${formatBalance(highestProfit.toString())}",
                    subValue = "Tek Satışta",
                    chartColor = Color(0xFFFFD700)
                )
                // Başarı Oranı
                StatBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.ThumbUp,
                    iconTint = Color(0xFF4DB6AC),
                    title = "Pazarlık",
                    value = "%$successRate",
                    subValue = "Başarı Oranı",
                    chartColor = Color(0xFF4DB6AC)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Toplam Satış
                StatBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.ShoppingCart,
                    iconTint = Color(0xFF64B5F6),
                    title = "Toplam Satış",
                    value = "$itemsSold",
                    subValue = "Ürün",
                    chartColor = Color(0xFF64B5F6)
                )
                // Alınan Ürün
                StatBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Inventory,
                    iconTint = Color(0xFFAB47BC),
                    title = "Alınan Ürün",
                    value = "$itemsBought",
                    subValue = "Adet",
                    chartColor = Color(0xFFAB47BC)
                )
                // En Çok Satan Kategori
                StatBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Category,
                    iconTint = Color(0xFFFFA726),
                    title = "Favori",
                    value = bestCategory.take(8) + if(bestCategory.length > 8) ".." else "",
                    subValue = "Kategori",
                    chartColor = Color(0xFFFFA726)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // DÜKKAN YOLCULUĞU BÖLÜMÜ
        item {
            Text("DÜKKAN YOLCULUĞU", color = MarketTextSecondary, fontSize = 12.sp, letterSpacing = 1.sp, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    DukkanCard(
                        title = "Sokak",
                        status = if (playerState.shopLevel > 1) "Gecildi" else "Mevcut",
                        levelReq = 1,
                        isUnlocked = true
                    )
                }
                item {
                    DukkanCard(
                        title = "Dükkan",
                        status = if (playerState.shopLevel > 2) "Gecildi" else if (playerState.shopLevel == 2) "Mevcut" else "Kilitli",
                        levelReq = 2,
                        isUnlocked = playerState.shopLevel >= 2
                    )
                }
                item {
                    DukkanCard(
                        title = "Galeri",
                        status = if (playerState.shopLevel >= 3) "Mevcut" else "Kilitli",
                        levelReq = 3,
                        isUnlocked = playerState.shopLevel >= 3
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Dükkanı Yükselt Kartı
            val shopCost = viewModel.getShopUpgradeCost(playerState.shopLevel)
            val canAfford = (playerState.balance.toDoubleOrNull() ?: 0.0) >= shopCost
            val isMaxed = playerState.shopLevel >= 3

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Card, RoundedCornerShape(16.dp))
                    .border(1.dp, MarketBorderSoft, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(CardSecondary, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Yükselt", tint = ReputationGold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Dükkanı Yükselt", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("Daha iyi ürünlere eriş ve kârını artır!", color = MarketTextSecondary, fontSize = 12.sp)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Yükseltme Maliyeti", color = MarketTextSecondary, fontSize = 10.sp)
                            Text(if (isMaxed) "MAX" else "₺${formatBalance(shopCost.toString())}", color = ReputationGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.upgradeShop() },
                        enabled = !isMaxed && canAfford,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF45D86E), disabledContainerColor = CardSecondary),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Upgrade", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isMaxed) "MAKSİMUM SEVİYE" else "Dükkanı Yükselt", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(32.dp)) }

        // KUPA ODASI (BAŞARIMLAR)
        item {
            Text("KAZANILAN BAŞARIMLAR", color = MarketTextSecondary, fontSize = 12.sp, letterSpacing = 1.sp, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))

            val unlockedIds = playerState.unlockedAchievements.split(",").filter { it.isNotEmpty() }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(AchievementManager.ALL_ACHIEVEMENTS.size) { index ->
                    val ach = AchievementManager.ALL_ACHIEVEMENTS[index]
                    val isUnlocked = unlockedIds.contains(ach.id)

                    if (ach.isHidden && !isUnlocked) {
                        BasarimCard(
                            title = "Gizemli Görev",
                            description = "Nasıl açılacağı bilinmiyor...",
                            icon = Icons.Default.HelpOutline,
                            isUnlocked = false
                        )
                    } else {
                        BasarimCard(
                            title = ach.title,
                            description = ach.description,
                            icon = ach.iconRes,
                            isUnlocked = isUnlocked
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    if (showSettings) {
        SettingsDialog(
            onDismiss = { showSettings = false },
            isSoundEnabled = isSoundEnabled,
            onSoundToggle = { listingViewModel.setSoundEnabled(it) },
            isHapticEnabled = isHapticEnabled,
            onHapticToggle = { listingViewModel.setHapticEnabled(it) },
            isQuickSellEnabled = isQuickSellEnabled,
            onQuickSellToggle = { listingViewModel.setFastSellEnabled(it) }
        )
    }
}

@Composable
fun StatBox(modifier: Modifier, icon: ImageVector, iconTint: Color, title: String, value: String, subValue: String, chartColor: Color) {
    Box(
        modifier = modifier
            .height(140.dp)
            .background(Card, RoundedCornerShape(16.dp))
            .border(1.dp, MarketBorderSoft, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(36.dp).background(CardSecondary, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = MarketTextSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = if(title == "Toplam Kâr") MoneyGreen else TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(subValue, color = iconTint, fontSize = 10.sp)
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Dummy Mini Chart
            Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
                val path = Path()
                val width = size.width
                val height = size.height
                path.moveTo(0f, height * 0.8f)
                path.lineTo(width * 0.2f, height * 0.9f)
                path.lineTo(width * 0.4f, height * 0.5f)
                path.lineTo(width * 0.6f, height * 0.7f)
                path.lineTo(width * 0.8f, height * 0.2f)
                path.lineTo(width, height * 0.4f)
                
                drawPath(
                    path = path,
                    color = chartColor,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun DukkanCard(title: String, status: String, levelReq: Int, isUnlocked: Boolean) {
    val borderColor = if (status == "Mevcut") ReputationGold else MarketBorderSoft
    
    Box(
        modifier = Modifier
            .width(110.dp)
            .height(150.dp)
            .background(Card, RoundedCornerShape(12.dp))
            .border(if (status == "Mevcut") 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
            Text(title, color = if(status == "Mevcut") ReputationGold else if(isUnlocked) MoneyGreen else MarketTextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Placeholder for Shop Image (Using Icon for now)
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(CardSecondary, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(if(title == "Sokak") Icons.Default.Storefront else if(title == "Dükkan") Icons.Default.Store else Icons.Default.Domain, contentDescription = null, tint = if(isUnlocked) TextPrimary else MarketTextSecondary)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (status == "Gecildi") {
                Icon(Icons.Default.CheckCircle, contentDescription = "Geçildi", tint = MoneyGreen, modifier = Modifier.size(24.dp))
            } else if (status == "Mevcut") {
                Box(modifier = Modifier.background(ReputationGold, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("Seviye $levelReq", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Mevcut Seviye", color = ReputationGold, fontSize = 10.sp)
            } else {
                Icon(Icons.Default.Lock, contentDescription = "Kilitli", tint = MarketTextSecondary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Seviye $levelReq", color = MarketTextSecondary, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun BasarimCard(title: String, description: String, icon: ImageVector, isUnlocked: Boolean) {
    val borderColor = if (isUnlocked) ReputationGold else MarketBorderSoft
    val bgColor = if (isUnlocked) Card else SurfaceVariant

    Box(
        modifier = Modifier
            .width(180.dp)
            .height(120.dp)
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).background(CardSecondary, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = if(isUnlocked) ReputationGold else MarketTextSecondary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    color = if(isUnlocked) TextPrimary else MarketTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(description, color = MarketTextSecondary, fontSize = 10.sp, lineHeight = 14.sp)
            Spacer(modifier = Modifier.weight(1f))
            if (isUnlocked) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Kazanıldı", tint = MoneyGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kazanıldı", color = MoneyGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = "Kilitli", tint = MarketTextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kilitli", color = MarketTextSecondary, fontSize = 10.sp)
                }
            }
        }
    }
}
