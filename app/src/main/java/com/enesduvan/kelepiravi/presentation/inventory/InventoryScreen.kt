package com.enesduvan.kelepiravi.presentation.inventory

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.enesduvan.kelepiravi.R
import com.enesduvan.kelepiravi.data.market.MarketGenerator
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.viewmodel.bargain.BargainViewModel
import com.enesduvan.kelepiravi.viewmodel.listing.ListingViewModel
import com.enesduvan.kelepiravi.viewmodel.MarketViewModel
import com.enesduvan.kelepiravi.ui.shared.EmptyStateIndicator
import com.enesduvan.kelepiravi.ui.shared.formatBalance
import com.enesduvan.kelepiravi.ui.shared.getPainterResourceByName
import com.enesduvan.kelepiravi.ui.shared.marketItemKey
import com.enesduvan.kelepiravi.ui.shared.bounceClick
import com.enesduvan.kelepiravi.ui.localization.localized
import com.enesduvan.kelepiravi.ui.theme.*
import kotlin.math.abs

val RED = Color(0xFFE53935)
private val RED_BG = Color(0x22FF6B6B)
private val GREEN_BG = Color(0x2254D548)
private val RED_DARK = Color(0xFF3A1A1A)
private val GREEN_DARK = Color(0xFF1A3A1A)

@Composable
fun InventoryScreen(marketViewModel: MarketViewModel, bargainViewModel: BargainViewModel, listingViewModel: ListingViewModel) {
    val playerState by marketViewModel.playerState.collectAsState()
    val isFastSell by listingViewModel.isFastSellEnabled.collectAsState()
    val activeListings by listingViewModel.activeListings.collectAsState()
    
    val inventoryItems = playerState.inventory
    val roi = playerState.portfolioROI
    val roiPositive = roi >= 0
    var itemForListing by remember { mutableStateOf<MarketItem?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().weight(1f),
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
Text(localized("Envanter", "Inventory"), color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
Text(localized("Satın aldığın fırsatlar.", "The bargains you bought."), color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(SurfaceVariant).padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📅", fontSize = 14.sp)
Text(localized("Gün ${playerState.currentDay}", "Day ${playerState.currentDay}"), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
Text(localized("Portföy Özeti", "Portfolio Summary"), color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PortfolioStatBox("Yatırım", "₺${formatBalance(playerState.totalInvestment.toString())}", TextPrimary, Modifier.weight(1f))
                                    PortfolioStatBox("Değer", "₺${formatBalance(playerState.portfolioValue.toString())}", MoneyGreen, Modifier.weight(1f))
                                    val roiSign = if (roiPositive) "+" else ""
                                    PortfolioStatBox("Kâr", "$roiSign${"%.1f".format(roi)}%", if (roiPositive) MoneyGreen else RED, Modifier.weight(1f))
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
                    items(items = inventoryItems, key = { marketItemKey(it) }) { item ->
                        val isListed = activeListings.any { it.item.id == item.id }
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
                                isFastSell = isFastSell,
                                isListed = isListed,
                                onActionClick = { 
                                    if (!isListed) {
                                        if (isFastSell) {
                                            bargainViewModel.startSellBargain(it)
                                        } else {
                                            itemForListing = it
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
    }

    if (itemForListing != null) {
        CreateListingDialog(
            item = itemForListing!!,
            onDismiss = { itemForListing = null },
            onConfirm = { price ->
                listingViewModel.addListing(itemForListing!!, price)
                itemForListing = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingDialog(item: MarketItem, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val baseValue = item.estimatedValue.toDouble()
    val currentMultiplier = MarketGenerator.getConditionMultiplier(item.condition) ?: 1.0
    val estimatedValue = baseValue * currentMultiplier
    val purchasePrice = (if ((item.purchasePrice.toLong() ?: 0L) > 0L) item.purchasePrice else item.salesValue).toDouble()
    
    var priceStr by remember { mutableStateOf(estimatedValue.toLong().toString()) }
    val price = priceStr.toDoubleOrNull() ?: 0.0
    val tax = price * 0.05
    val netProfit = price - purchasePrice - tax

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MarketplaceBackground)
        ) {
            // Header (Sabit)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                IconButton(onClick = onDismiss) {
Icon(Icons.Default.ArrowBack, contentDescription = localized("Geri", "Back"), tint = TextPrimary)
                }
Text(localized("Satışa Koy", "List for Sale"), color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
Icon(Icons.Default.HelpOutline, contentDescription = localized("Yardım", "Help"), tint = MarketTextSecondary)
            }

            // Kaydırılabilir İçerik
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {

            // Item Card
            Box(
                modifier = Modifier.fillMaxWidth().background(Card, RoundedCornerShape(16.dp))
                    .border(1.dp, MarketBorderSoft, RoundedCornerShape(16.dp)).padding(16.dp)
            ) {
                Row {
                    Box(modifier = Modifier.size(100.dp).background(CardSecondary, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Image(painter = getPainterResourceByName(item.imageName), contentDescription = null, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(localized(item.itemName), color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        val (badgeBg, badgeText) = when {
                            item.condition.contains("Kusursuz") || item.condition.contains("Temiz") -> ConditionPerfectBg to ConditionPerfect
                            item.condition.contains("Tamir") || item.condition.contains("Bantlı")   -> ConditionRepairBg to ConditionRepair
                            else -> ConditionScratchBg to ConditionScratch
                        }
                        Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(badgeBg).padding(horizontal = 8.dp, vertical = 2.dp)) {
                            Text(localized(item.condition), color = badgeText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
Text(localized("Sıfır/Kusursuz Değeri: ₺${formatBalance(baseValue.toString())}", "New/Perfect Value: ₺${formatBalance(baseValue.toString())}"), color = MarketTextSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
Text(localized("Güncel Hasarlı Değeri", "Current Damaged Value"), color = MarketTextSecondary, fontSize = 12.sp)
                        Text("₺${formatBalance(estimatedValue.toString())}", color = MoneyGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
Text(localized("Envanterdeki Adet", "Quantity in Inventory"), color = MarketTextSecondary, fontSize = 12.sp)
                        Text("1", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Price Set Card
            Box(
                modifier = Modifier.fillMaxWidth().background(Card, RoundedCornerShape(16.dp))
                    .border(1.dp, MarketBorderSoft, RoundedCornerShape(16.dp)).padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(localized("FİYAT BELİRLE", "SET PRICE"), color = MarketTextSecondary, fontSize = 12.sp, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
Text(localized("Önerilen Fiyat", "Suggested Price"), color = MarketTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Info, contentDescription = null, tint = MarketTextSecondary, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        IconButton(onClick = { priceStr = ((price - 50).coerceAtLeast(0.0)).toLong().toString() }, modifier = Modifier.size(40.dp).background(CardSecondary, RoundedCornerShape(20.dp))) {
                            Text("-", color = TextPrimary, fontSize = 24.sp)
                        }
                        
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            BasicTextField(
                                value = priceStr,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isDigit() } && newValue.length <= 12) {
                                        priceStr = newValue
                                    }
                                },
                                textStyle = TextStyle(
                                    color = MoneyGreen,
                                    fontSize = if (priceStr.length > 7) 24.sp else 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                decorationBox = { innerTextField ->
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                        Text("₺", color = MoneyGreen, fontSize = if (priceStr.length > 7) 24.sp else 32.sp, fontWeight = FontWeight.Bold)
                                        innerTextField()
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                        
                        IconButton(onClick = { priceStr = (price + 50).toLong().toString() }, modifier = Modifier.size(40.dp).background(CardSecondary, RoundedCornerShape(20.dp))) {
                            Text("+", color = TextPrimary, fontSize = 24.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("₺${formatBalance(estimatedValue * 0.5)}", color = MarketTextSecondary, fontSize = 10.sp)
                        Text("₺${formatBalance(estimatedValue * 1.5)}", color = MarketTextSecondary, fontSize = 10.sp)
                    }
                    Slider(
                        value = price.toFloat(),
                        onValueChange = { priceStr = it.toLong().toString() },
                        valueRange = (estimatedValue * 0.1).toFloat().coerceAtLeast(0f)..(estimatedValue * 3.0).toFloat(),
                        colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = MoneyGreen, inactiveTrackColor = CardSecondary)
                    )
                    
                    val sellProbability = when {
                        price <= estimatedValue * 0.5 -> "🔥 Çok Yüksek"
                        price <= estimatedValue * 0.8 -> "✅ Yüksek"
                        price <= estimatedValue * 1.1 -> "🟡 Normal"
                        price <= estimatedValue * 1.5 -> "🟠 Düşük"
                        else -> "🔴 Çok Düşük"
                    }
                    val probColor = when {
                        price <= estimatedValue * 0.8 -> MoneyGreen
                        price <= estimatedValue * 1.1 -> PrimaryOrange
                        else -> RED
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
Text(localized("Satılma Olasılığı: ", "Sale Probability: "), color = MarketTextSecondary, fontSize = 12.sp)
                        Text(sellProbability, color = probColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Profit Box
            Box(
                modifier = Modifier.fillMaxWidth().background(Card, RoundedCornerShape(16.dp))
                    .border(1.dp, MoneyGreen.copy(0.3f), RoundedCornerShape(16.dp)).padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).background(MoneyGreen.copy(0.2f), RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                            Text("₺", color = MoneyGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
Text(localized("Tahmini Net Kârın", "Estimated Net Profit"), color = MarketTextSecondary, fontSize = 14.sp)
                            Text("₺${formatBalance(netProfit.toString())}", color = MoneyGreen, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MoneyGreen, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
Text(localized("Doğru fiyatla hızlıca sat ve daha çok kazan!", "Sell quickly at the right price and earn more!"), color = MarketTextSecondary, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MarketBorderSoft)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MarketTextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
Text(localized("Alış Fiyatı", "Purchase Price"), color = MarketTextSecondary, fontSize = 14.sp)
                        }
                        Text("₺${formatBalance(purchasePrice.toString())}", color = MarketTextSecondary, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = MarketTextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
Text(localized("Vergi (%5)", "Tax (5%)"), color = MarketTextSecondary, fontSize = 14.sp)
                        }
                        Text("-₺${formatBalance(tax.toString())}", color = MarketTextSecondary, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MoneyGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
Text(localized("Tahmini Net Kâr", "Estimated Net Profit"), color = MoneyGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("₺${formatBalance(netProfit.toString())}", color = MoneyGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            } // Kaydırılabilir Alan Sonu

            // Sabit Alt Alan
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Card)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { onConfirm(price.toLong().toString()) },
                    colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.LocalOffer, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
Text(localized("Satışa Koy", "List for Sale"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MoneyGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
Text(localized("Ürün satışa konulduğunda envanterden düşer.", "The item leaves your inventory when listed for sale."), color = MarketTextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

// ─── Yardımcı Composable'lar (Eskiden kalanlar) ───────────────────────────────────────────────────────

@Composable
private fun PortfolioStatBox(title: String, value: String, valueColor: Color, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, textAlign = TextAlign.Center)
    }
}

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

@Composable
private fun InventoryItemCard(item: MarketItem, isFastSell: Boolean, isListed: Boolean = false, onActionClick: (MarketItem) -> Unit) {
    val (badgeBg, badgeText) = when {
        item.condition.contains("Kusursuz") || item.condition.contains("Temiz") -> ConditionPerfectBg to ConditionPerfect
        item.condition.contains("Tamir") || item.condition.contains("Bantlı")   -> ConditionRepairBg to ConditionRepair
        else -> ConditionScratchBg to ConditionScratch
    }
    val purchasePrice = (if (item.purchasePrice > 0L) item.purchasePrice else item.salesValue).toDouble()
    val baseValue = item.estimatedValue.toDouble()
    val currentMultiplier = MarketGenerator.getConditionMultiplier(item.condition) ?: 1.0
    val estimatedValue = baseValue * currentMultiplier
    val profit = estimatedValue - purchasePrice
    val isProfit = profit >= 0
    val wasScam = item.isScammer  

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
        if (hasDailyChange) {
            Box(
                modifier = Modifier.fillMaxWidth().height(3.dp)
                    .background(if (isDailyPositive) MoneyGreen else RED)
            )
        }
        if (wasScam && !hasDailyChange) {
            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFFF4444)))
        }

        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
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
                if (hasDailyChange) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${if (isDailyPositive) "▲" else "▼"}${abs(dailyChange).toInt()}%",
                            color = if (isDailyPositive) MoneyGreen else RED,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(localized(item.itemName), color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(badgeBg).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text(localized(item.condition), color = badgeText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    if (isListed) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(PrimaryOrange.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                            Text(localized("İlanda", "Listed"), color = PrimaryOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (wasScam) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xFF3A1A00)).padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(localized("⚠️ Kazıklandın!", "⚠️ You got scammed!"), color = Color(0xFFFF8C00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text("${localized("Alış:", "Purchase:")} ₺${formatBalance(purchasePrice)}", color = TextSecondary, fontSize = 12.sp)
                Text("${localized("Kusursuz:", "Perfect:")} ₺${formatBalance(baseValue)}", color = MarketTextSecondary, fontSize = 11.sp)
                Text("${localized("Güncel:", "Current:")} ₺${formatBalance(estimatedValue)}", color = MoneyGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${if (isProfit) "+" else ""}${formatBalance(profit)}₺",
                    color = if (isProfit) MoneyGreen else RED,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isProfit) GREEN_BG else RED_BG)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { if (!isListed) onActionClick(item) },
                    enabled = !isListed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListed) PrimaryOrange.copy(alpha = 0.4f) else PrimaryOrange
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = if (isListed) localized("İlanda", "Listed") else localized(if (isFastSell) "Pazarlık Yap" else "İlan Ver"),
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
