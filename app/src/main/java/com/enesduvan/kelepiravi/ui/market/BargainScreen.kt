package com.enesduvan.kelepiravi.ui.market

import androidx.activity.compose.BackHandler
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BargainScreen(viewModel: MarketViewModel, bargainState: BargainState) {
    val listState = rememberLazyListState()
    var offerText by remember { mutableStateOf(bargainState.suggestedPrice.toInt().toString()) }

    // Otomatik aşağı kaydırma
    LaunchedEffect(bargainState.messages.size) {
        if (bargainState.messages.isNotEmpty()) {
            listState.animateScrollToItem(bargainState.messages.size - 1)
        }
    }

    // Geri tuşuna basıldığında
    BackHandler {
        viewModel.closeBargain()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Pazarlık", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.closeBargain() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        bottomBar = {
            if (bargainState.isDealClosed) {
                // Anlaşma sağlandı, Satın Al butonu
                Column(
                    modifier = Modifier
                        .background(Surface)
                        .navigationBarsPadding()
                        .imePadding()
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF1A3A1A)).border(1.dp, MoneyGreen.copy(0.4f), RoundedCornerShape(12.dp)).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Icon omitted, text only is fine
                            Text("Anlaştınız!\n₺${formatBalance(bargainState.agreedPrice.toString())}", color = MoneyGreen, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.buyAgreedItem() },
                            colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Satın Al", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (bargainState.isFailed) {
                // Satıcı masadan kalktı
                Column(
                    modifier = Modifier
                        .background(Surface)
                        .navigationBarsPadding()
                        .imePadding()
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Satıcı masadan kalktı.", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.closeBargain() },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Geri Dön", color = TextPrimary)
                    }
                }
            } else {
                // Teklif giriş alanı
                Column(
                    modifier = Modifier
                        .background(Surface)
                        .navigationBarsPadding()
                        .imePadding()
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Teklifinizi girin", color = TextSecondary, fontSize = 13.sp)
                        Text("Önerilen: ₺${formatBalance(bargainState.suggestedPrice.toString())}", color = MoneyGreen, fontSize = 13.sp)
                    }

                    OutlinedTextField(
                        value = offerText,
                        onValueChange = { offerText = it.filter { char -> char.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Text("₺", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = SurfaceVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = PrimaryOrange
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { QuickOfferButton("-500") { val v = offerText.toIntOrNull() ?: 0; offerText = maxOf(0, v - 500).toString() } }
                        item { QuickOfferButton("-100") { val v = offerText.toIntOrNull() ?: 0; offerText = maxOf(0, v - 100).toString() } }
                        item { QuickOfferButton("-50") { val v = offerText.toIntOrNull() ?: 0; offerText = maxOf(0, v - 50).toString() } }
                        item { QuickOfferButton("+50") { val v = offerText.toIntOrNull() ?: 0; offerText = (v + 50).toString() } }
                        item { QuickOfferButton("+100") { val v = offerText.toIntOrNull() ?: 0; offerText = (v + 100).toString() } }
                        item { QuickOfferButton("+250") { val v = offerText.toIntOrNull() ?: 0; offerText = (v + 250).toString() } }
                        item { QuickOfferButton("+500") { val v = offerText.toIntOrNull() ?: 0; offerText = (v + 500).toString() } }
                    }

                    if (bargainState.lastSellerOffer != null && !bargainState.isDealClosed && !bargainState.isFailed) {
                        Button(
                            onClick = {
                                viewModel.sendOffer(bargainState.lastSellerOffer)
                            },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen)
                        ) {
                            Text("Kabul Et (₺${formatBalance(bargainState.lastSellerOffer.toString())})", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            val offer = offerText.toDoubleOrNull() ?: 0.0
                            if (offer > 0) {
                                viewModel.sendOffer(offer)
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
                        "Teklifleriniz sadece siz ve satıcı tarafından görülür.",
                        color = TextSecondary, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // ── Üst Kart: Ürün ve Satıcı Durumu ──────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Ürün Görseli
                Box(
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = getPainterResourceByName(bargainState.item.imageName),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(bargainState.item.itemName, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(painterResource(id = R.drawable.person), contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        val personality = SellerPersonality.fromName(bargainState.item.sellerName)
                        Text("${bargainState.item.sellerName} [${personality.title}]", color = TextSecondary, fontSize = 13.sp)
                    }
                    Text("₺${formatBalance(bargainState.item.salesValue)}", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text("İlan Fiyatı", color = TextSecondary, fontSize = 11.sp)
                }

                // Satıcı Modu (Sabır / Patience)
                val emoji = when (bargainState.sellerMood) {
                    "Mutlu" -> "😊"
                    "Kararsız" -> "😐"
                    "Gergin" -> "😠"
                    else -> "😡"
                }
                val moodColor = when (bargainState.sellerMood) {
                    "Mutlu" -> MoneyGreen
                    "Kararsız" -> PrimaryOrange
                    "Gergin" -> Color(0xFFFF6B6B)
                    else -> Color(0xFFD32F2F)
                }

                Column(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Surface).border(1.dp, SurfaceVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Satıcı Modu", color = TextSecondary, fontSize = 10.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(emoji, fontSize = 20.sp)
                        Text(bargainState.sellerMood, color = moodColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    // Basit sabır barı
                    Row(modifier = Modifier.width(60.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(SurfaceVariant)) {
                        Box(modifier = Modifier.fillMaxWidth(bargainState.sellerPatience / 100f).fillMaxHeight().background(moodColor))
                    }
                }
            }

            // Güvenli Pazarlık Ayırıcı
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(SurfaceVariant))
                Text("   Güvenli Pazarlık   ", color = TextSecondary, fontSize = 12.sp)
                Box(modifier = Modifier.weight(1f).height(1.dp).background(SurfaceVariant))
            }

            // ── Chat Alanı ──────────────────────────────────────────
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = bargainState.messages, key = { it.id }) { msg ->
                    ChatBubble(msg)
                }
            }
        }
    }
}

@Composable
fun QuickOfferButton(text: String, onClick: () -> Unit) {
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
fun ChatBubble(msg: BargainMessage) {
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
