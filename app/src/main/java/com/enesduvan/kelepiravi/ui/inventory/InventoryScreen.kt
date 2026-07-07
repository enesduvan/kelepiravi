package com.enesduvan.kelepiravi.ui.inventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enesduvan.kelepiravi.R
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.ui.market.MarketViewModel
import com.enesduvan.kelepiravi.ui.shared.EmptyStateIndicator
import com.enesduvan.kelepiravi.ui.shared.formatBalance
import com.enesduvan.kelepiravi.ui.shared.getPainterResourceByName
import com.enesduvan.kelepiravi.ui.shared.marketItemKey
import com.enesduvan.kelepiravi.ui.theme.*

private val RED = Color(0xFFFF6B6B)
private val RED_BG = Color(0x22FF6B6B)
private val GREEN_BG = Color(0x2254D548)
private val RED_DARK = Color(0xFF3A1A1A)
private val GREEN_DARK = Color(0xFF1A3A1A)

@Composable
fun InventoryScreen(viewModel: MarketViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val inventoryItems = playerState.inventory
    val roi = playerState.portfolioROI
    val roiPositive = roi >= 0

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // ── Başlık ──────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Envanter", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Satın aldığın fırsatlar.", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
                }
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(SurfaceVariant).padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📅", fontSize = 14.sp)
                        Text("Gün ${playerState.currentDay}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Portföy Özeti Kartı ──────────────────────────────────────────────
            if (inventoryItems.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Surface)
                        .border(
                            1.dp,
                            if (roiPositive) MoneyGreen.copy(0.3f) else RED.copy(0.3f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Portföy Özeti", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PortfolioStatBox("Yatırım", "₺${formatBalance(playerState.totalInvestment.toString())}", TextPrimary, Modifier.weight(1f))
                            PortfolioStatBox("Değer", "₺${formatBalance(playerState.portfolioValue.toString())}", MoneyGreen, Modifier.weight(1f))
                            val roiSign = if (roiPositive) "+" else ""
                            PortfolioStatBox("ROI", "$roiSign${"%.1f".format(roi)}%", if (roiPositive) MoneyGreen else RED, Modifier.weight(1f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // ── Stat Kutuları ────────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val maxCapacity = 5 + (playerState.shopLevel * 5)
                InventoryStatBox("Depo Kapasitesi", "${inventoryItems.size} / $maxCapacity", TextPrimary, Modifier.weight(1f))
                val totalProfit = playerState.portfolioValue - playerState.totalInvestment
                InventoryStatBox(
                    "Kâr/Zarar",
                    "${if (totalProfit >= 0) "+" else ""}₺${formatBalance(totalProfit.toString())}",
                    if (totalProfit >= 0) MoneyGreen else RED,
                    Modifier.weight(2f)
                )
            }
        }

        if (inventoryItems.isEmpty()) {
            item {
                EmptyStateIndicator(
                    iconRes = R.drawable.envanter,
                    title = "Envanterin Tam Takır!",
                    description = "Hemen Market'e git ve kelepir eşyalar avla.",
                    modifier = Modifier.height(300.dp)
                )
            }
        } else {
            // Ch6: Animasyonlu kart girişleri
            items(items = inventoryItems, key = { marketItemKey(it) }) { item ->
                var isVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { isVisible = true }
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(280)) + slideInVertically(
                        animationSpec = tween(320, easing = EaseOutCubic),
                        initialOffsetY = { it / 4 }
                    )
                ) {
                    InventoryItemCard(
                        item = item,
                        sellPrice = viewModel.getSellPrice(item),
                        onSellClick = { viewModel.startSellBargain(it) }
                    )
                }
            }
        }
    }
}

// ─── Portföy Stat Kutusu ───────────────────────────────────────────────────────

@Composable
private fun PortfolioStatBox(title: String, value: String, valueColor: Color, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, textAlign = TextAlign.Center)
    }
}

// ─── Envanter Stat Kutusu ──────────────────────────────────────────────────────

@Composable
private fun InventoryStatBox(title: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    val animatedColor by animateColorAsState(targetValue = valueColor, animationSpec = tween(300), label = "statColor")
    Column(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(Surface)
            .border(1.dp, BorderSoft, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(title, color = TextSecondary, fontSize = 11.sp)
        Text(value, color = animatedColor, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

// ─── Envanter Kartı ───────────────────────────────────────────────────────────

@Composable
private fun InventoryItemCard(item: MarketItem, sellPrice: Double, onSellClick: (MarketItem) -> Unit) {
    val (badgeBg, badgeText) = when {
        item.condition.contains("Kusursuz") || item.condition.contains("Temiz") -> ConditionPerfectBg to ConditionPerfect
        item.condition.contains("Tamir") || item.condition.contains("Bantlı")   -> ConditionRepairBg to ConditionRepair
        else -> ConditionScratchBg to ConditionScratch
    }
    val purchasePrice = item.purchasePrice.ifEmpty { item.salesValue }.toDoubleOrNull() ?: 0.0
    val estimatedValue = item.estimatedValue.toDoubleOrNull() ?: 0.0
    val profit = estimatedValue - purchasePrice
    val isProfit = profit >= 0
    val wasScam = item.isScammer  // Ch6: Dolandırıcıdan alındı mı?

    // Günlük değişim
    val dailyChange = item.dailyChangePercent
    val hasDailyChange = dailyChange != 0.0
    val isDailyPositive = dailyChange > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (wasScam) Color(0xFF1C0A0A) else Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        // Günlük değişim şeridi
        if (hasDailyChange) {
            Box(
                modifier = Modifier.fillMaxWidth().height(3.dp)
                    .background(if (isDailyPositive) MoneyGreen else RED)
            )
        }
        // Ch6: Dolandırıcı kırmızı şerit
        if (wasScam && !hasDailyChange) {
            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFFF4444)))
        }

        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Görsel
            Box(
                modifier = Modifier.size(92.dp).clip(RoundedCornerShape(16.dp)).background(SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = getPainterResourceByName(item.imageName),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Günlük değişim overlay
                if (hasDailyChange) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDailyPositive) GREEN_DARK else RED_DARK)
                            .border(1.dp, if (isDailyPositive) MoneyGreen.copy(0.4f) else RED.copy(0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${if (isDailyPositive) "▲" else "▼"} ${"%.1f".format(kotlin.math.abs(dailyChange))}%",
                            color = if (isDailyPositive) MoneyGreen else RED,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Bilgiler
            Column(modifier = Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.itemName, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(badgeBg).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text(item.condition, color = badgeText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                // Ch6: Dolandırıcı badge'i
                if (wasScam) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xFF3A1A00)).padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("⚠️ Kazıklandın!", color = Color(0xFFFF8C00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text("Alış: ₺${formatBalance(purchasePrice.toString())}", color = TextSecondary, fontSize = 12.sp)

                // Değer + kâr/zarar inline
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("₺${formatBalance(estimatedValue.toString())}", color = MoneyGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${if (isProfit) "+" else ""}${formatBalance(profit.toString())}₺",
                        color = if (isProfit) MoneyGreen else RED,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clip(RoundedCornerShape(6.dp))
                            .background(if (isProfit) GREEN_BG else RED_BG)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }

                if (item.purchaseDate.isNotEmpty()) {
                    Text(item.purchaseDate, color = TextMuted, fontSize = 10.sp)
                }
            }

            // Butonlar
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onSellClick(item) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) { Text("Pazarlık Yap", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold) }

                Button(
                    onClick = { /* Tamir ekranında yapılıyor */ },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) { Text("Tamir", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
