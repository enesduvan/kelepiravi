package com.enesduvan.kelepiravi.ui.market

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enesduvan.kelepiravi.data.market.LootBoxType
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.ui.shared.formatBalance
import com.enesduvan.kelepiravi.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LootBoxBottomSheet(
    playerBalance: Double,
    inventorySize: Int,
    shopLevel: Int,
    onDismiss: () -> Unit,
    onBuy: (LootBoxType) -> Unit
) {
    val maxCapacity = 5 + (shopLevel * 5)
    // Kutu açılımında 2-3 eşya gelebilir, +3 limit kontrolü yapalım
    val isFull = inventorySize + 3 > maxCapacity

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BottomSheet,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 16.dp)
                    .width(48.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Handle)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Zamazon Paletleri",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "İçinden ne çıkacağı belli olmayan kapalı paletler! Bazen hurda, bazen altın madeni.",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isFull) {
                Text(
                    text = "Dükkanında yeterli yer yok! (En az 3 boş yer gerekli)",
                    color = Color(0xFFFF6B6B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            LootBoxType.values().forEach { boxType ->
                val canAfford = playerBalance >= boxType.price
                val canBuy = canAfford && !isFull
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(enabled = canBuy) { onBuy(boxType) }
                        .border(
                            width = 1.dp,
                            color = if (canBuy) DealGreen.copy(alpha = 0.5f) else BorderSoft,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (canBuy) Card else CardSecondary
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📦",
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = boxType.title,
                                color = if (canBuy) DealGreen else TextSecondary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = boxType.description,
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₺${formatBalance(boxType.price.toString())}",
                                color = if (canBuy) TextPrimary else ErrorRed,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LootBoxRevealScreen(
    items: List<MarketItem>,
    onDismiss: () -> Unit
) {
    // Animasyon state'leri
    var showBox by remember { mutableStateOf(false) }
    var boxExploded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showBox = true
        delay(1200) // Gerilim süresi
        boxExploded = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE605070C)) // Çok koyu yarı şeffaf zemin
            .clickable(enabled = boxExploded) { onDismiss() }, // Sadece patladıktan sonra kapatılabilir
        contentAlignment = Alignment.Center
    ) {
        if (!boxExploded) {
            AnimatedVisibility(
                visible = showBox,
                enter = scaleIn(animationSpec = tween(500)),
                exit = scaleOut(animationSpec = tween(200))
            ) {
                // Sadece kutu simgesi
                Text(
                    text = "📦",
                    fontSize = 120.sp,
                    modifier = Modifier.padding(bottom = 50.dp)
                )
            }
        } else {
            // Çıkan eşyaların listesi
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
            ) {
                Text(
                    "Palet Açıldı!",
                    color = DealGreen,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                LazyColumn {
                    items(items) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = Card),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CardSecondary)
                                ) {
                                    val context = androidx.compose.ui.platform.LocalContext.current
                                    val resId = remember(item.imageName) {
                                        context.resources.getIdentifier(item.imageName, "drawable", context.packageName)
                                    }
                                    if (resId != 0) {
                                        androidx.compose.foundation.Image(
                                            painter = androidx.compose.ui.res.painterResource(id = resId),
                                            contentDescription = item.itemName,
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text("🎁", fontSize = 24.sp, modifier = Modifier.align(Alignment.Center))
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(item.itemName, color = TextPrimary, fontWeight = FontWeight.Bold)
                                    Text("Durum: ${item.condition}", color = TextSecondary, fontSize = 12.sp)
                                    Text("Değer: ₺${formatBalance(item.estimatedValue)}", color = MoneyGreen, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Devam etmek için ekrana dokun",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
        }
    }
}
