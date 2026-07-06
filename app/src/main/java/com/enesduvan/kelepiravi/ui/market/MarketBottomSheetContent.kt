package com.enesduvan.kelepiravi.ui.market

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enesduvan.kelepiravi.R
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.ui.shared.formatBalance
import com.enesduvan.kelepiravi.ui.shared.getPainterResourceByName
import com.enesduvan.kelepiravi.ui.theme.BalanceGreen
import com.enesduvan.kelepiravi.ui.theme.BuyButton
import com.enesduvan.kelepiravi.ui.theme.BuyButtonText
import com.enesduvan.kelepiravi.ui.theme.CloseBg
import com.enesduvan.kelepiravi.ui.theme.CloseIcon
import com.enesduvan.kelepiravi.ui.theme.ConditionPerfect
import com.enesduvan.kelepiravi.ui.theme.ConditionPerfectBg
import com.enesduvan.kelepiravi.ui.theme.ConditionRepair
import com.enesduvan.kelepiravi.ui.theme.ConditionRepairBg
import com.enesduvan.kelepiravi.ui.theme.ConditionScratch
import com.enesduvan.kelepiravi.ui.theme.ConditionScratchBg
import com.enesduvan.kelepiravi.ui.theme.EstimatedValue
import com.enesduvan.kelepiravi.ui.theme.ImageFrame
import com.enesduvan.kelepiravi.ui.theme.MarketBorderSoft
import com.enesduvan.kelepiravi.ui.theme.MarketTextPrimary
import com.enesduvan.kelepiravi.ui.theme.MarketTextSecondary
import com.enesduvan.kelepiravi.ui.theme.PriceText
import com.enesduvan.kelepiravi.ui.theme.TipBg
import com.enesduvan.kelepiravi.ui.theme.TipBorder

/**
 * Ürün detay bottom sheet içeriği.
 * ViewModel'den bağımsız — saf composable, sadece callback alır.
 *
 * @param item Gösterilecek ürün
 * @param playerBalance Oyuncunun güncel bakiyesi (ViewModel'den gelir)
 * @param onClose Bottom sheet kapatma callback'i
 * @param onPurchase Satın alma callback'i — iş mantığı ViewModel'de
 */
@Composable
fun MarketBottomSheetContent(
    item: MarketItem,
    playerBalance: String,
    inventorySize: Int,
    shopLevel: Int,
    onClose: () -> Unit,
    onPurchase: () -> Unit
) {
    val guncelBakiyeStr = formatBalance(playerBalance)

    val (badgeBg, badgeText) = when {
        item.condition.contains("Kusursuz") || item.condition.contains("Temiz") -> ConditionPerfectBg to ConditionPerfect
        item.condition.contains("Tamir") || item.condition.contains("Bantlı") -> ConditionRepairBg to ConditionRepair
        else -> ConditionScratchBg to ConditionScratch
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── 1. Görsel ve Bilgiler ─────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .weight(0.40f)
                    .aspectRatio(0.85f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ImageFrame),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = getPainterResourceByName(item.imageName),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier.weight(0.60f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = item.itemName, color = MarketTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)

                Box(
                    modifier = Modifier
                        .background(badgeBg, shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = item.condition, color = badgeText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_person), contentDescription = null, tint = MarketTextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Satıcı: ${item.sellerName}", color = MarketTextSecondary, fontSize = 12.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_trending), contentDescription = null, tint = MarketTextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Tahmini değer: ", color = MarketTextSecondary, fontSize = 12.sp)
                    Text(text = "${item.estimatedValue}₺", color = EstimatedValue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .drawBehind {
                            drawRoundRect(
                                color = MarketBorderSoft,
                                style = Stroke(
                                    width = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                ),
                                cornerRadius = CornerRadius(8.dp.toPx())
                            )
                        }
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(painter = painterResource(id = R.drawable.ic_tag), contentDescription = null, tint = BalanceGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Bu ürün piyasaya göre yaklaşık %20 ucuz görünüyor!", color = MarketTextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── 2. Fiyat ve Bakiye Kutusu ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MarketBorderSoft, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Fiyat", color = MarketTextSecondary, fontSize = 12.sp)
                    Text(text = "₺ ${item.salesValue}", color = PriceText, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_money), contentDescription = null, tint = BalanceGreen, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(text = "Bakiye", color = MarketTextSecondary, fontSize = 12.sp)
                        Text(text = "₺ $guncelBakiyeStr", color = BalanceGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── 3. İpucu Kutusu ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TipBg, shape = RoundedCornerShape(16.dp))
                .border(1.dp, TipBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_lightbulb), contentDescription = null, tint = BalanceGreen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = "İpucu", color = BalanceGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.itemName} tamir edilirse veya satılırsa güzel kâr bırakabilir.",
                    color = MarketTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── 4. Aksiyon Butonları ──────────────────────────────────────────────
        val maxCapacity = 5 + (shopLevel * 5)
        val isFull = inventorySize >= maxCapacity

        if (isFull) {
            Text(
                text = "Dükkan kapasiten dolu! ($inventorySize/$maxCapacity)",
                color = Color(0xFFFF6B6B),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Button(
            onClick = onPurchase,
            enabled = !isFull,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFull) Color(0xFF4A4A4A) else BuyButton,
                disabledContainerColor = Color(0xFF4A4A4A)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_cart), contentDescription = null, tint = if (isFull) Color(0xFFAAAAAA) else Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Satın Al", color = if (isFull) Color(0xFFAAAAAA) else BuyButtonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { /* Pazarlık Yap — Sprint 2 */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .border(1.dp, BalanceGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "Pazarlık Yap", color = BalanceGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = BalanceGreen)
        }

        // ── 5. Kapat Butonu ───────────────────────────────────────────────────
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(CloseBg, CircleShape)
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_close), contentDescription = null, tint = CloseIcon)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
