package com.enesduvan.kelepiravi.presentation.market

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enesduvan.kelepiravi.data.event.EventChoice
import com.enesduvan.kelepiravi.data.event.EventDefinition
import com.enesduvan.kelepiravi.data.market.ScamType
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.viewmodel.DailySummaryState
import com.enesduvan.kelepiravi.presentation.lootbox.LootBoxBottomSheet
import com.enesduvan.kelepiravi.presentation.lootbox.LootBoxRevealScreen
import com.enesduvan.kelepiravi.viewmodel.MarketViewModel
import com.enesduvan.kelepiravi.presentation.seller.SellerProfileDialog
import com.enesduvan.kelepiravi.ui.shared.formatBalance
import com.enesduvan.kelepiravi.ui.shared.marketItemKey
import com.enesduvan.kelepiravi.ui.shared.bounceClick
import com.enesduvan.kelepiravi.ui.localization.localized
import com.enesduvan.kelepiravi.presentation.market.components.*
import com.enesduvan.kelepiravi.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    viewModel: MarketViewModel,
    onItemClick: (MarketItem) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val dailySummary by viewModel.dailySummary.collectAsState()
    val scamReveal by viewModel.scamReveal.collectAsState()
    val interactiveEvent by viewModel.interactiveEvent.collectAsState()
    val eventResult by viewModel.eventResult.collectAsState()
    val flashNotification by viewModel.flashNotification.collectAsState()

    val balanceText = remember(playerState.balance) { formatBalance(playerState.balance) }
    val visibleItems by remember(uiState.marketItems, uiState.selectedCategory, uiState.searchQuery) {
        derivedStateOf { viewModel.filteredMarketItems(uiState) }
    }

    // Ch6: Bakiye değişince animasyonlu renk yanıp sönmesi
    val balanceColor by animateColorAsState(
        targetValue = Color.White,
        animationSpec = tween(300),
        label = "balance"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Background,
            topBar = {
                Column(modifier = Modifier.background(Background)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("₺$balanceText", color = balanceColor, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                            if (playerState.inventory.isNotEmpty()) {
                                val roi = playerState.portfolioROI
                                val roiColor = if (roi >= 0) MoneyGreen else Color(0xFFFF6B6B)
                                val roiSign = if (roi >= 0) "+" else ""
                                Text(
                                    localized("Portföy Kâr: ${roiSign}${String.format(Locale.US, "%.2f", roi)}%", "Portfolio Profit: ${roiSign}${String.format(Locale.US, "%.2f", roi)}%"),
                                    color = roiColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Button(
                            onClick = { viewModel.advanceDay() },
                            enabled = !uiState.isDayAdvancing,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isDayAdvancing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                            } else {
                                Text(localized("Yeni Gün (${playerState.currentDay})", "New Day (${playerState.currentDay})"), color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = flashNotification != null,
                        enter = expandVertically(animationSpec = tween(400, easing = EaseOutBack)) + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        flashNotification?.let { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                                    .background(if (msg.contains("Maalesef")) Color(0xFFFF4444) else PrimaryOrange, RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = localized(msg),
                                    color = Color.Black,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Text(
                        localized("Sokak Satıcısı", "Street Trader"),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )

                    // V6.0: Arama Çubuğu
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        placeholder = { Text(localized("Ne arıyorsunuz?", "What are you looking for?"), color = TextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = localized("Ara", "Search"), tint = TextSecondary) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = localized("Temizle", "Clear"), tint = TextSecondary)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = SurfaceVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = PrimaryOrange,
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    val categoriesList = remember {
                        listOf("Tümü", "Elektronik", "Ev Aletleri", "Giyim", "Spor", "Koleksiyon")
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = categoriesList, key = { it }) { category ->
                            val isSelected = uiState.selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectCategory(category) },
                                label = {
                                    Text(
                                        localized(category),
                                        color = if (isSelected) Color.Black else TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryOrange,
                                    containerColor = Surface
                                ),
                                border = null,
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            },
            floatingActionButton = {
                // Compact Zamazon Box FAB
                FloatingActionButton(
                    onClick = { viewModel.setLootBoxSheetVisible(true) },
                    containerColor = FAB,
                    contentColor = FABIcon,
                    shape = CircleShape,
                    modifier = Modifier.size(52.dp)
                ) {
                    Text("📦", fontSize = 24.sp)
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 86.dp)
            ) {
                items(items = visibleItems, key = { marketItemKey(it) }) { item ->
                    ItemCard(
                        item = item,
                        onClick = { onItemClick(it) }
                    )
                }
            }
        }

        // Günlük özet dialog
        AnimatedVisibility(
            visible = dailySummary != null,
            enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.85f),
            exit = fadeOut(tween(200)) + scaleOut(tween(200))
        ) {
            if (dailySummary != null) {
                DailySummaryDialog(summary = dailySummary!!, onDismiss = { viewModel.dismissDailySummary() })
            }
        }

        // Ch6: Interaktif Event (Olay Motoru)
        AnimatedVisibility(
            visible = interactiveEvent != null,
            enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f),
            exit = fadeOut(tween(200)) + scaleOut(tween(200))
        ) {
            if (interactiveEvent != null) {
                InteractiveEventDialog(
                    event = interactiveEvent!!,
                    onChoiceSelected = { choice -> viewModel.applyInteractiveEventChoice(choice) }
                )
            }
        }

        // Ch6: Interaktif Event Sonucu
        AnimatedVisibility(
            visible = eventResult != null,
            enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f),
            exit = fadeOut(tween(200)) + scaleOut(tween(200))
        ) {
            eventResult?.let { resultText ->
                EventResultDialog(
                    resultText = resultText,
                    onDismiss = { viewModel.dismissEventResult() }
                )
            }
        }

        if (uiState.isLootBoxSheetOpen) {
            val balance = playerState.balance.toDouble()
            LootBoxBottomSheet(
                playerBalance = balance,
                inventorySize = playerState.inventory.size,
                shopLevel = playerState.shopLevel,
                onDismiss = { viewModel.setLootBoxSheetVisible(false) },
                onBuy = { type -> viewModel.buyLootBox(type) }
            )
        }

        if (uiState.purchasedLootBoxItems != null) {
            LootBoxRevealScreen(
                items = uiState.purchasedLootBoxItems!!,
                onDismiss = { viewModel.dismissLootBoxReveal() }
            )
        }

        AnimatedVisibility(
            visible = scamReveal != null,
            enter = fadeIn(tween(250)) + scaleIn(tween(350), initialScale = 0.8f),
            exit = fadeOut(tween(200)) + scaleOut(tween(200))
        ) {
            scamReveal?.let { item ->
                ScamRevealDialog(item = item, onDismiss = { viewModel.dismissScamReveal() })
            }
        }

        // V6.0: Satıcı Profili Dialogu
        val sellerProfile by viewModel.sellerProfile.collectAsState()
        AnimatedVisibility(
            visible = sellerProfile != null,
            enter = fadeIn(tween(250)) + scaleIn(tween(350), initialScale = 0.8f),
            exit = fadeOut(tween(200)) + scaleOut(tween(200))
        ) {
            sellerProfile?.let { profile ->
                SellerProfileDialog(
                    profile = profile,
                    onDismiss = { viewModel.closeSellerProfile() },
                    onItemClick = { item ->
                        viewModel.closeSellerProfile()
                        viewModel.startBargain(item)
                    }
                )
            }
        }
    }
}

// Ch6: Dolandırıcı Reveal Dialogu
@Composable
fun ScamRevealDialog(item: MarketItem, onDismiss: () -> Unit) {
    val scamType = try {
        ScamType.valueOf(item.scamType)
    } catch (e: Exception) { null }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.88f).padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0A0A))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("💀", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(localized("KAZIKLANDIN!", "YOU GOT SCAMMED!"), color = Color(0xFFFF4444), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2A0A0A))
                        .border(1.dp, Color(0xFFFF4444).copy(0.4f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (scamType != null) {
                            Text(localized(scamType.warningText), color = Color(0xFFFFB74D), fontSize = 13.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(localized(scamType.revealText), color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            localized("Gerçek Durum: ${item.hiddenCondition}", "Actual Condition: ${item.hiddenCondition}"),
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    localized("Zamanla bu satıcıları tanımayı öğreneceksin. Her kazıklanma bir deneyimdir! 💪", "You will learn to recognize these sellers over time. Every scam is a lesson! 💪"),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(localized("Anladım 😤", "Got it 😤"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DailySummaryDialog(summary: DailySummaryState, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f).padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${summary.day}. ${localized("Gün Özeti", "Day Summary")}", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(localized("Giriş Bonusu:", "Login Bonus:"), color = TextSecondary, fontSize = 14.sp)
                    Text("+₺${formatBalance(summary.bonusMoney.toString())}", color = MoneyGreen, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(localized("Kazanılan XP:", "XP Earned:"), color = TextSecondary, fontSize = 14.sp)
                    Text("+${summary.xpGained} XP", color = PrimaryOrange, fontWeight = FontWeight.Bold)
                }

                if (summary.rentPaid > 0 || summary.taxPaid > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = SurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(localized("Giderler", "Expenses"), color = ErrorRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (summary.rentPaid > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(localized("Dükkan Kirası:", "Shop Rent:"), color = TextSecondary, fontSize = 14.sp)
                            Text("-₺${formatBalance(summary.rentPaid.toString())}", color = ErrorRed, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    if (summary.taxPaid > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(localized("Satış Vergisi (%5):", "Sales Tax (5%):"), color = TextSecondary, fontSize = 14.sp)
                            Text("-₺${formatBalance(summary.taxPaid.toString())}", color = ErrorRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                if (summary.event != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = SurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(localized("📰 Günün Haberi", "📰 Daily News"), color = PrimaryOrange, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(localized(summary.event.title), color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(localized(summary.event.description), color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) {
                    Text(localized("Güne Başla", "Start the Day"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


/*
@Preview
@Composable
fun PreviewMarketScreen() {
    val dummyViewModel = MarketViewModel(repository = com.enesduvan.kelepiravi.data.repository.KelepiraviRepository(database = com.enesduvan.kelepiravi.database.AppDatabaseProvider.getDatabase(androidx.compose.ui.platform.LocalContext.current), context = androidx.compose.ui.platform.LocalContext.current), settingsManager = com.enesduvan.kelepiravi.data.local.SettingsManager(androidx.compose.ui.platform.LocalContext.current), soundManager = com.enesduvan.kelepiravi.ui.shared.SoundManager(androidx.compose.ui.platform.LocalContext.current, com.enesduvan.kelepiravi.data.local.SettingsManager(androidx.compose.ui.platform.LocalContext.current).isSoundEnabled))
    KelepiraviTheme {
        MarketScreen(viewModel = dummyViewModel)
    }
}
*/
