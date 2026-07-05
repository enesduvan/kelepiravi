package com.enesduvan.kelepiravi.ui.inventory

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.enesduvan.kelepiravi.ui.shared.formatBalance
import com.enesduvan.kelepiravi.ui.shared.getPainterResourceByName
import com.enesduvan.kelepiravi.ui.theme.Background
import com.enesduvan.kelepiravi.ui.theme.BorderSoft
import com.enesduvan.kelepiravi.ui.theme.ConditionPerfect
import com.enesduvan.kelepiravi.ui.theme.ConditionPerfectBg
import com.enesduvan.kelepiravi.ui.theme.ConditionRepair
import com.enesduvan.kelepiravi.ui.theme.ConditionRepairBg
import com.enesduvan.kelepiravi.ui.theme.ConditionScratch
import com.enesduvan.kelepiravi.ui.theme.ConditionScratchBg
import com.enesduvan.kelepiravi.ui.theme.MoneyGreen
import com.enesduvan.kelepiravi.ui.theme.PrimaryOrange
import com.enesduvan.kelepiravi.ui.theme.Surface
import com.enesduvan.kelepiravi.ui.theme.SurfaceVariant
import com.enesduvan.kelepiravi.ui.theme.TextMuted
import com.enesduvan.kelepiravi.ui.theme.TextPrimary
import com.enesduvan.kelepiravi.ui.theme.TextSecondary

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

    var itemToSell by remember { mutableStateOf<MarketItem?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // ── Başlık ───────────────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Envanter", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Satın aldığın fırsatlar.", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
                }
                // Gün sayacı küçük
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

            // ── Portföy Özeti Kartı ───────────────────────────────────────────
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

            // ── Stat Kutuları ─────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InventoryStatBox("Ürün", inventoryItems.size.toString(), TextPrimary, Modifier.weight(1f))
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
            item { EmptyInventory() }
        } else {
            items(items = inventoryItems) { item ->
                InventoryItemCard(item = item, onSellClick = { itemToSell = item })
            }
        }
    }

    // ── Satış Onay Dialog'u ───────────────────────────────────────────────────
    itemToSell?.let { item ->
        val sellPrice = viewModel.getSellPrice(item)
        val purchasePrice = item.purchasePrice.ifEmpty { item.salesValue }.toDoubleOrNull() ?: 0.0
        val profit = sellPrice - purchasePrice
        val isProfit = profit >= 0

        AlertDialog(
            onDismissRequest = { itemToSell = null },
            containerColor = Surface,
            title = { Text("Satışı Onayla", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.itemName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Kondisyon: ${item.condition}", color = TextSecondary, fontSize = 14.sp)
                    Text("Alış: ₺${formatBalance(purchasePrice.toString())}", color = TextSecondary, fontSize = 14.sp)
                    Text("Satış fiyatı: ₺${formatBalance(sellPrice.toString())}", color = MoneyGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(if (isProfit) GREEN_DARK else RED_DARK).padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isProfit) "Kâr" else "Zarar", color = if (isProfit) MoneyGreen else RED, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            "${if (isProfit) "+" else ""}₺${formatBalance(profit.toString())}",
                            color = if (isProfit) MoneyGreen else RED, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp
                        )
                    }
                    if (item.purchaseDate.isNotEmpty()) {
                        Text("Alış tarihi: ${item.purchaseDate}", color = TextMuted, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.sellItem(item); itemToSell = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Sat", color = Color.Black, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { itemToSell = null }) { Text("Vazgeç", color = TextSecondary) }
            }
        )
    }
}

// ─── Portföy Stat Kutusu ──────────────────────────────────────────────────────

@Composable
private fun PortfolioStatBox(title: String, value: String, valueColor: Color, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, textAlign = TextAlign.Center)
    }
}

// ─── Envanter Stat Kutusu ─────────────────────────────────────────────────────

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

// ─── Boş Envanter ────────────────────────────────────────────────────────────

@Composable
private fun EmptyInventory() {
    Box(
        modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(20.dp))
            .background(Surface).border(1.dp, BorderSoft, RoundedCornerShape(20.dp)).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(painter = painterResource(id = R.drawable.envanter), contentDescription = null, tint = TextMuted, modifier = Modifier.size(42.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Henüz ürün yok", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Pazardan bir ürün alınca burada görünecek.", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

// ─── Envanter Kartı ──────────────────────────────────────────────────────────

@Composable
private fun InventoryItemCard(item: MarketItem, onSellClick: () -> Unit) {
    val (badgeBg, badgeText) = when {
        item.condition.contains("Kusursuz") || item.condition.contains("Temiz") -> ConditionPerfectBg to ConditionPerfect
        item.condition.contains("Tamir") || item.condition.contains("Bantlı")   -> ConditionRepairBg to ConditionRepair
        else -> ConditionScratchBg to ConditionScratch
    }
    val purchasePrice = item.purchasePrice.ifEmpty { item.salesValue }.toDoubleOrNull() ?: 0.0
    val estimatedValue = item.estimatedValue.toDoubleOrNull() ?: 0.0
    val profit = estimatedValue - purchasePrice
    val isProfit = profit >= 0

    // Günlük değişim
    val dailyChange = item.dailyChangePercent
    val hasDailyChange = dailyChange != 0.0
    val isDailyPositive = dailyChange > 0

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
        // Günlük değişim şeridi — kart üstünde renkli ince bar
        if (hasDailyChange) {
            Box(
                modifier = Modifier.fillMaxWidth().height(3.dp)
                    .background(if (isDailyPositive) MoneyGreen else RED)
            )
        }

        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Görsel
            Box(
                modifier = Modifier.size(92.dp).clip(RoundedCornerShape(16.dp)).background(SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Image(painter = getPainterResourceByName(item.imageName), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                // Günlük değişim overlay
                if (hasDailyChange) {
                    Box(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)
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
                    onClick = onSellClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) { Text("Sat", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold) }

                Button(
                    onClick = { /* Tamir — Sprint 5 */ },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) { Text("Tamir", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
