package com.enesduvan.kelepiravi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable // Tıklama özelliği için gerekli import
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind // Kesik çizgiler için gerekli import
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Tema Renkleri
import com.enesduvan.kelepiravi.ui.theme.BalanceGreen
import com.enesduvan.kelepiravi.ui.theme.BuyButton
import com.enesduvan.kelepiravi.ui.theme.BuyButtonText
import com.enesduvan.kelepiravi.ui.theme.CloseBg
import com.enesduvan.kelepiravi.ui.theme.CloseIcon
import com.enesduvan.kelepiravi.ui.theme.ConditionBrokenBg
import com.enesduvan.kelepiravi.ui.theme.ConditionBrokenText
import com.enesduvan.kelepiravi.ui.theme.ConditionPerfect
import com.enesduvan.kelepiravi.ui.theme.ConditionPerfectBg
import com.enesduvan.kelepiravi.ui.theme.ConditionRepair
import com.enesduvan.kelepiravi.ui.theme.ConditionRepairBg
import com.enesduvan.kelepiravi.ui.theme.ConditionScratch
import com.enesduvan.kelepiravi.ui.theme.ConditionScratchBg
import com.enesduvan.kelepiravi.ui.theme.EstimatedValue
import com.enesduvan.kelepiravi.ui.theme.ImageFrame
import com.enesduvan.kelepiravi.ui.theme.KelepiraviTheme
import com.enesduvan.kelepiravi.ui.theme.MarketBorderSoft
import com.enesduvan.kelepiravi.ui.theme.MarketTextPrimary
import com.enesduvan.kelepiravi.ui.theme.MarketTextSecondary
import com.enesduvan.kelepiravi.ui.theme.NegotiateBg
import com.enesduvan.kelepiravi.ui.theme.NegotiateText
import com.enesduvan.kelepiravi.ui.theme.PriceText
import com.enesduvan.kelepiravi.ui.theme.TipBg
import com.enesduvan.kelepiravi.ui.theme.TipBorder

class MarketBottomSheet : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KelepiraviTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting2(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting2(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun ProductDetailBottomSheetContent(item: MarketItem, onClose: () -> Unit) {
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
        // --- 1. ÜST KISIM: GÖRSEL VE BİLGİLER YAN YANA ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Sol Taraf: Ürün Görseli (Padding tamamen silindi, Crop yapıldı!)
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
                    contentScale = ContentScale.Crop, // Kutunun içini boşluksuz tam doldurur
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Sağ Taraf: Detaylar
            Column(
                modifier = Modifier.weight(0.60f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.itemName,
                    color = MarketTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp
                )

                // Dinamik Kondisyon Badge'i
                Box(
                    modifier = Modifier
                        .background(badgeBg, shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = item.condition, color = badgeText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Satıcı Bilgisi
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_person), contentDescription = null, tint = MarketTextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Satıcı: ${item.sellerName}", color = MarketTextSecondary, fontSize = 12.sp)
                }

                // Tahmini Değer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_trending), contentDescription = null, tint = MarketTextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Tahmini değer: ", color = MarketTextSecondary, fontSize = 12.sp)
                    Text(text = "${item.estimatedValue}₺", color = EstimatedValue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // %20 Ucuz Kutusu
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
                        Text(
                            text = "Bu ürün piyasaya göre yaklaşık %20 ucuz görünüyor!",
                            color = MarketTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. FİYAT VE BAKİYE KUTUSU ---
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
                    // Bakiye yazıları sola hizalandı (Alignment.Start)
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(text = "Bakiye", color = MarketTextSecondary, fontSize = 12.sp)
                        Text(text = "₺24.850", color = BalanceGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 3. İPUCU KUTUSU ---
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

        // --- 4. AKSİYON BUTONLARI ---
        Button(
            onClick = { /* Satın alma işlemleri */ },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BuyButton),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_cart), contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Satın Al", color = BuyButtonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { /* Pazarlık Yap */ },
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
            Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = BalanceGreen)
        }

        // --- 5. KAPATMA (X) BUTONU ---
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview3() {
    val mockPreviewItem = MarketItem(
        condition = "Kırık Ekran",
        sellerName = "Sabırsız Murat",
        itemName = "Akıllı Telefon",
        salesValue = "3600",
        estimatedValue = "4500",
        imageName = "telefon"
    )
    KelepiraviTheme {
        ProductDetailBottomSheetContent(item = mockPreviewItem, onClose = { })
    }
}