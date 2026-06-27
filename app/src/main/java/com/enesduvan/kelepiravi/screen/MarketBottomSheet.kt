package com.enesduvan.kelepiravi.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enesduvan.kelepiravi.MarketItem
import com.enesduvan.kelepiravi.R
import com.enesduvan.kelepiravi.database.DEFAULT_USER_ID
import com.enesduvan.kelepiravi.database.DatabaseKelepiravi
import com.enesduvan.kelepiravi.database.INITIAL_BALANCE
import com.enesduvan.kelepiravi.database.KelepiraviDatabaseProvider
import kotlinx.coroutines.launch

// Tema Renkleri
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
import com.enesduvan.kelepiravi.ui.theme.KelepiraviTheme
import com.enesduvan.kelepiravi.ui.theme.MarketBorderSoft
import com.enesduvan.kelepiravi.ui.theme.MarketTextPrimary
import com.enesduvan.kelepiravi.ui.theme.MarketTextSecondary
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
fun ProductDetailBottomSheetContent(
    item: MarketItem,
    onClose: () -> Unit,
    onPurchaseSuccess: (MarketItem) -> Unit = {}
) {
    // Veritabanı ve Coroutine Tanımlamaları
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val kelepiraviDao = remember(context) {
        KelepiraviDatabaseProvider.getDatabase(context).kelepiraviDao()
    }

    // Veritabanındaki değişimleri canlı dinlemek için Flow -> State dönüşümü
    val allInventories by kelepiraviDao.getAllInventories().collectAsState(initial = emptyList())
    val currentUserData = allInventories.firstOrNull { it.id == DEFAULT_USER_ID }

    // Veritabanından gelen anlık bakiye (Eğer veri henüz yoksa varsayılan olarak 25000.0 gösterir)
    val guncelBakiyeStr = formatBalance(currentUserData?.balance ?: INITIAL_BALANCE)

    // Kondisyon Badge Renk Belirleme Mantığı
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
            // Sol Taraf: Ürün Görseli
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

        // --- 2. FİYAT VE BAKİYE KUTUSU (DİNAMİKLEŞTİRİLDİ) ---
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
                        // Bakiye artık veritabanındaki canlı durumu gösteriyor
                        Text(text = "₺ $guncelBakiyeStr", color = BalanceGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
        // ASIL SATIN ALMA BUTONU
        Button(
            onClick = {
                coroutineScope.launch {
                    val userId = DEFAULT_USER_ID
                    val urunFiyati = item.salesValue.toDoubleOrNull() ?: 0.0

                    var mevcutVeri = kelepiraviDao.getInventoryById(userId)

                    // Veritabanı ilk kez açıldıysa ve boşsa, varsayılan bir kullanıcı kaydı açıyoruz
                    if (mevcutVeri == null) {
                        val ilkKullanici = DatabaseKelepiravi(id = userId, balance = INITIAL_BALANCE, inventory = emptyList())
                        kelepiraviDao.insertInventory(ilkKullanici)
                        mevcutVeri = ilkKullanici
                    }

                    val suAnkiBakiye = mevcutVeri.balance.toDoubleOrNull() ?: 0.0

                    if (suAnkiBakiye >= urunFiyati) {
                        val yeniBakiye = suAnkiBakiye - urunFiyati
                        val yeniEnvanterListesi = mevcutVeri.inventory.toMutableList()
                        yeniEnvanterListesi.add(item)

                        val guncelVeri = mevcutVeri.copy(
                            balance = yeniBakiye.toString(),
                            inventory = yeniEnvanterListesi
                        )

                        // Veritabanı güncelleniyor (Ekrandaki bakiye otomatik olarak değişecek)
                        kelepiraviDao.updateInventory(guncelVeri)
                        onPurchaseSuccess(item)

                        // Satın alma bitince sayfayı kapatmak için tetikliyoruz
                        onClose()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BuyButton),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_cart), contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Satın Al", color = BuyButtonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // PAZARLIK YAP BUTONU
        Button(
            onClick = { /* Pazarlık Yap İşlemleri */ },
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
