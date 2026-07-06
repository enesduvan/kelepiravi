package com.enesduvan.kelepiravi.ui.market

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enesduvan.kelepiravi.R
import com.enesduvan.kelepiravi.data.market.SellerPersonality
import com.enesduvan.kelepiravi.ui.shared.formatBalance
import com.enesduvan.kelepiravi.ui.shared.getPainterResourceByName
import com.enesduvan.kelepiravi.ui.theme.*

private val RED = Color(0xFFFF6B6B)
private val RED_DARK = Color(0xFF3A1A1A)
private val GREEN_DARK = Color(0xFF1A3A1A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellBargainScreen(viewModel: MarketViewModel, sellBargainState: SellBargainState) {
    var offerText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Ch6: Animasyonlu sabır barı
    val animatedPatience by animateFloatAsState(
        targetValue = sellBargainState.buyerPatience / 100f,
        animationSpec = tween(500, easing = EaseOutCubic),
        label = "patience"
    )

    // Ekran açıldığında son mesaja scroll
    LaunchedEffect(sellBargainState.messages.size) {
        if (sellBargainState.messages.isNotEmpty()) {
            listState.animateScrollToItem(sellBargainState.messages.size - 1)
        }
    }

    BackHandler { viewModel.closeSellBargain() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pazarlık Masası", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.closeSellBargain() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        containerColor = Background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(Surface)
                    .navigationBarsPadding()
                    .imePadding()
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (sellBargainState.isDealClosed) {
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(GREEN_DARK).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🤝 Anlaşma Sağlandı!", color = MoneyGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Anlaşılan Fiyat: ₺${formatBalance(sellBargainState.agreedPrice.toString())}", color = Color.White, fontSize = 14.sp)
                        Button(
                            onClick = { viewModel.sellAgreedItem() },
                            colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Eşyayı Sat", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (sellBargainState.isFailed) {
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(RED_DARK).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💥 Pazarlık Çöktü", color = RED, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Alıcı masadan kalktı. Artık bu kişiye satamazsınız.", color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center)
                        Button(
                            onClick = { viewModel.closeSellBargain() },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Geri Dön", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    val currentBase = sellBargainState.baseSellPrice
                    val lastOffer = sellBargainState.lastBuyerOffer

                    if (lastOffer != null) {
                        Button(
                            onClick = { viewModel.sendSellOffer(lastOffer) },
                            modifier = Modifier.fillMaxWidth().height(48.dp).padding(bottom = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GREEN_DARK),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Teklifi Kabul Et: ₺${formatBalance(lastOffer.toString())}", color = MoneyGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SellQuickOfferButton("₺${formatBalance((currentBase).toString())}") {
                            viewModel.sendSellOffer(currentBase)
                        }
                        SellQuickOfferButton("₺${formatBalance((currentBase * 1.05).toString())}") {
                            viewModel.sendSellOffer(currentBase * 1.05)
                        }
                        SellQuickOfferButton("₺${formatBalance((currentBase * 1.15).toString())}") {
                            viewModel.sendSellOffer(currentBase * 1.15)
                        }
                    }

                    OutlinedTextField(
                        value = offerText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) offerText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Kendi teklifini gir...", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = BorderSoft,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val offer = offerText.toDoubleOrNull()
                            if (offer != null && offer > 0) {
                                offerText = ""
                                viewModel.sendSellOffer(offer)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Teklif Gönder", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Text(
                        "Teklifleriniz sadece siz ve alıcı tarafından görülür.",
                        color = TextSecondary, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // ── Üst Kart: Ürün ve Alıcı Durumu ──────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = getPainterResourceByName(sellBargainState.item.imageName),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(sellBargainState.item.itemName, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(painterResource(id = R.drawable.person), contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        val personality = SellerPersonality.fromName(sellBargainState.buyerName)
                        Text("${sellBargainState.buyerName} [${personality.title}]", color = TextSecondary, fontSize = 13.sp)
                    }
                    Text("₺${formatBalance(sellBargainState.baseSellPrice.toString())}", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Değeri", color = TextSecondary, fontSize = 11.sp)
                }

                // Ch6: Alıcı Modu — animasyonlu
                val emoji = when (sellBargainState.buyerMood) {
                    "Mutlu" -> "😊"
                    "Kararsız" -> "🤔"
                    "Gergin" -> "😬"
                    else -> "😡"
                }
                val moodColor by animateColorAsState(
                    targetValue = when (sellBargainState.buyerMood) {
                        "Mutlu" -> MoneyGreen
                        "Kararsız" -> PrimaryOrange
                        "Gergin" -> Color(0xFFFF6B6B)
                        else -> Color(0xFFD32F2F)
                    },
                    animationSpec = tween(400),
                    label = "moodColor"
                )

                Column(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Surface)
                        .border(1.dp, SurfaceVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Alıcı Modu", color = TextSecondary, fontSize = 10.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(emoji, fontSize = 20.sp)
                        Text(sellBargainState.buyerMood, color = moodColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    // Animasyonlu sabır barı
                    Box(modifier = Modifier.width(60.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(SurfaceVariant)) {
                        Box(modifier = Modifier.fillMaxWidth(animatedPatience).fillMaxHeight().background(moodColor))
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(SurfaceVariant))
                Text("   Güvenli Satış Pazarlığı   ", color = TextSecondary, fontSize = 12.sp)
                Box(modifier = Modifier.weight(1f).height(1.dp).background(SurfaceVariant))
            }

            // Ch6: Animasyonlu chat alanı
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = sellBargainState.messages, key = { it.id }) { msg ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(250)) + slideInVertically(
                            animationSpec = tween(300, easing = EaseOutCubic),
                            initialOffsetY = { it / 2 }
                        )
                    ) {
                        SellChatBubble(msg)
                    }
                }
            }
        }
    }
}

@Composable
fun SellQuickOfferButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp)
    ) {
        Text(text, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SellChatBubble(msg: BargainMessage) {
    val isPlayer = msg.isFromPlayer
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isPlayer) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isPlayer) {
            Box(modifier = Modifier.size(32.dp).clip(androidx.compose.foundation.shape.CircleShape).background(SurfaceVariant), contentAlignment = Alignment.Center) {
                Icon(painterResource(id = R.drawable.person), contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isPlayer) Alignment.End else Alignment.Start) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isPlayer) 16.dp else 4.dp,
                            bottomEnd = if (isPlayer) 4.dp else 16.dp
                        )
                    )
                    .background(if (isPlayer) PrimaryOrange else SurfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = msg.text,
                    color = if (isPlayer) Color.Black else TextPrimary,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(msg.timestamp, color = TextMuted, fontSize = 10.sp)
            }
        }
    }
}
