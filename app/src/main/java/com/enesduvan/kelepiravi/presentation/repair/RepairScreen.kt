package com.enesduvan.kelepiravi.presentation.repair

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enesduvan.kelepiravi.R
import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.data.market.MarketGenerator
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.viewmodel.MarketViewModel
import com.enesduvan.kelepiravi.viewmodel.repair.RepairResultState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.enesduvan.kelepiravi.ui.shared.formatBalance
import com.enesduvan.kelepiravi.ui.shared.getPainterResourceByName
import com.enesduvan.kelepiravi.ui.shared.EmptyStateIndicator
import com.enesduvan.kelepiravi.ui.shared.marketItemKey
import com.enesduvan.kelepiravi.ui.shared.bounceClick
import com.enesduvan.kelepiravi.ui.theme.*
import kotlinx.coroutines.delay
import com.enesduvan.kelepiravi.viewmodel.repair.RepairViewModel

@Composable
fun TamirEkrani(viewModel: RepairViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val repairResult by viewModel.repairResult.collectAsState()
    
    val repairableItems = playerState.inventory.filter { it.condition != "Kusursuz Temiz" }
    val remainingRepairs = viewModel.getRemainingRepairs()

    var selectedItem by remember { mutableStateOf<MarketItem?>(null) }
    var selectedOption by remember { mutableStateOf<String>("Cirak") } // "Cirak" veya "Usta"
    var isRepairing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Eğer başarılı tamir olduysa listeye dön
    LaunchedEffect(repairResult) {
        if (repairResult != null) {
            isRepairing = false
            selectedItem = null
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MarketplaceBackground)) {
        if (selectedItem == null) {
            // LİSTE GÖRÜNÜMÜ
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Tamir Atölyesi", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Eşyaları tamir ederek değerlerini artırabilirsiniz.", color = MarketTextSecondary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                DailyRepairCounter(remaining = remainingRepairs, total = GameConstants.DAILY_REPAIR_LIMIT)
                Spacer(modifier = Modifier.height(16.dp))

                if (repairableItems.isEmpty()) {
                    EmptyStateIndicator(iconRes = R.drawable.tamir, title = "İş Yok", description = "Envanterde bozuk eşya yok.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(items = repairableItems, key = { marketItemKey(it) }) { item ->
                            val currentVal = item.estimatedValue.toDouble()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Card)
                                    .border(1.dp, MarketBorderSoft, RoundedCornerShape(12.dp))
                                    .clickable { selectedItem = item }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(60.dp).background(CardSecondary, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                    Image(painter = getPainterResourceByName(item.imageName), contentDescription = null, modifier = Modifier.fillMaxSize())
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.itemName, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("Durum: ${item.condition}", color = ConditionScratch, fontSize = 12.sp)
                                    Text("Değer: ₺${formatBalance(currentVal)}", color = MoneyGreen, fontSize = 12.sp)
                                }
                                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = MarketTextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        } else {
            // DETAY GÖRÜNÜMÜ (MOCKUP 5a20)
            val item = selectedItem!!
            val baseVal = item.estimatedValue.toDouble()
            val currentMultiplier = MarketGenerator.getConditionMultiplier(item.condition) ?: 1.0
            val currentVal = baseVal * currentMultiplier
            val cirakCost = viewModel.calculateRepairCost(item, false)
            val ustaCost = viewModel.calculateRepairCost(item, true)
            val selectedCost = if (selectedOption == "Cirak") cirakCost else ustaCost
            val canAfford = playerState.balance.toDouble() >= selectedCost
            val canRepair = remainingRepairs > 0 && canAfford && !isRepairing
            
            val mechanicLevel = playerState.mechanicLevel
            val failureReduction = (mechanicLevel - 1) * 0.08
            val cirakFailure = (0.40 - failureReduction).coerceAtLeast(0.0)
            val cirakSuccess = ((1.0 - cirakFailure) * 100).toInt()
            val ustaSuccess = 98


            // Dinamik Metinler
            val (cirakTitle, cirakSub, ustaTitle, ustaSub) = when (item.category.lowercase()) {
                "emlak", "realestate", "ev" -> listOf("Ufak Tadilat", "Boya Badana", "Müteahhit", "Kapsamlı Restorasyon")
                "otomotiv", "vehicles", "araba" -> listOf("Sanayi Ustası", "Lokal Boya", "Yetkili Servis", "Orijinal Parça")
                else -> listOf("Çırak", "Ucuz ama riskli", "Usta", "Pahalı ama garantili")
            }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { selectedItem = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
                    }
                    Text("Tamirhane", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Ürün Kartı
                Box(modifier = Modifier.fillMaxWidth().background(Card, RoundedCornerShape(16.dp)).border(1.dp, MarketBorderSoft, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(80.dp).background(CardSecondary, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Image(painter = getPainterResourceByName(item.imageName), contentDescription = null, modifier = Modifier.fillMaxSize())
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(item.itemName, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            val (badgeBg, badgeText) = when {
                                item.condition.contains("Tamir") || item.condition.contains("Bantlı") || 
                                item.condition.contains("Arızalı") || item.condition.contains("Masraflı") || 
                                item.condition.contains("Yıkık") || item.condition.contains("Pert") -> ConditionRepairBg to ConditionRepair
                                else -> ConditionScratchBg to ConditionScratch
                            }
                            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(badgeBg).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                Text(item.condition, color = badgeText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Güncel Hasarlı Değeri: ₺${formatBalance(currentVal.toString())}", color = MarketTextSecondary, fontSize = 12.sp)
                            Text("Sıfır Değeri: ₺${formatBalance(baseVal.toString())}", color = MoneyGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Seçenekler
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // ÇIRAK
                    Box(
                        modifier = Modifier.weight(1f)
                            .bounceClick { selectedOption = "Cirak" }
                            .background(if(selectedOption == "Cirak") CardSecondary else Card, RoundedCornerShape(16.dp))
                            .border(2.dp, if(selectedOption == "Cirak") PrimaryOrange else MarketBorderSoft, RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(cirakTitle, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(cirakSub, color = MarketTextSecondary, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("₺${formatBalance(cirakCost.toString())}", color = PrimaryOrange, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color(0xFFFF4444), RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Başarı: %$cirakSuccess", color = Color(0xFFFF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    // USTA
                    Box(
                        modifier = Modifier.weight(1f)
                            .bounceClick { selectedOption = "Usta" }
                            .background(if(selectedOption == "Usta") CardSecondary else Card, RoundedCornerShape(16.dp))
                            .border(2.dp, if(selectedOption == "Usta") MoneyGreen else MarketBorderSoft, RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(ustaTitle, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(ustaSub, color = MarketTextSecondary, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("₺${formatBalance(ustaCost.toString())}", color = MoneyGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(MoneyGreen, RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Başarı: %$ustaSuccess", color = MoneyGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Satışa Son Etki Bilgisi
                val expectedResultCirak = when(item.category.lowercase()) {
                    "emlak", "realestate", "ev" -> "Masrafsız Temiz"
                    "otomotiv", "vehicles", "araba" -> "Lokal Boyalı"
                    else -> "Temiz"
                }
                val expectedResultUsta = when(item.category.lowercase()) {
                    "emlak", "realestate", "ev" -> "Sıfır / Ultra Lüks"
                    "otomotiv", "vehicles", "araba" -> "Hatasız Boyasız"
                    else -> "Kusursuz Temiz"
                }
                val expectedResult = if(selectedOption == "Cirak") expectedResultCirak else expectedResultUsta
                val expectedGain = baseVal
                Box(
                    modifier = Modifier.fillMaxWidth().background(Card, RoundedCornerShape(12.dp)).border(1.dp, MarketBorderSoft, RoundedCornerShape(12.dp)).padding(16.dp)
                ) {
                    Column {
                        Text("Satışa Son Etki", color = MarketTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Beklenen Durum:", color = TextPrimary, fontSize = 14.sp)
                            Text(expectedResult, color = if(selectedOption == "Usta") MoneyGreen else PrimaryOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tahmini Yeni Değer:", color = TextPrimary, fontSize = 14.sp)
                            Text("₺${formatBalance(expectedGain.toString())}", color = MoneyGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Tamir Butonu
                Button(
                    onClick = {
                        isRepairing = true
                        coroutineScope.launch {
                            delay(3000) // 3 saniye tornavida animasyonu beklemesi
                            viewModel.repairItem(item, selectedOption == "Usta")
                        }
                    },
                    enabled = canRepair,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange, disabledContainerColor = CardSecondary),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isRepairing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Tamir Ediliyor...", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Build, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tamire Başla - ₺${formatBalance(selectedCost.toString())}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // V4.0 Fullscreen Premium Loading Overlay
        AnimatedVisibility(
            visible = isRepairing,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = PrimaryOrange,
                        modifier = Modifier.size(72.dp),
                        strokeWidth = 6.dp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Usta İş Başında...",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Lütfen bekleyin, eşya onarılıyor.",
                        color = MarketTextSecondary,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Repair Result Dialog
        AnimatedVisibility(
            visible = repairResult != null,
            enter = fadeIn(tween(200)) + scaleIn(tween(300), initialScale = 0.8f),
            exit = fadeOut(tween(200)) + scaleOut(tween(200))
        ) {
            repairResult?.let { result ->
                RepairResultDialog(
                    result = result,
                    onDismiss = { viewModel.dismissRepairResult() }
                )
            }
        }
    }
}

// Ch6: Günlük tamir sayacı kartı
@Composable
fun DailyRepairCounter(remaining: Int, total: Int) {
    val fraction = remaining.toFloat() / total.toFloat()
    val barColor by animateColorAsState(
        targetValue = when {
            remaining == 0 -> Color(0xFFFF6B6B)
            remaining == 1 -> Color(0xFFFFB74D)
            else -> MoneyGreen
        },
        animationSpec = tween(400),
        label = "repairBarColor"
    )
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "repairFraction"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, if (remaining == 0) Color(0xFFFF6B6B).copy(0.5f) else SurfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔧", fontSize = 20.sp)
                    Text("Günlük Tamir Hakkı", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Text(
                    "$remaining / $total",
                    color = barColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Animasyonlu bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(SurfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFraction)
                        .fillMaxHeight()
                        .background(barColor)
                )
            }

            Text(
                if (remaining == 0) "⏰ Yarın 2 yeni tamir hakkın olacak"
                else "Her tamirde %10 başarısızlık riski vardır!",
                color = if (remaining == 0) Color(0xFFFF6B6B) else TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

// Ch6: Tamir Sonucu Dialogu
@Composable
fun RepairResultDialog(result: RepairResultState, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f).padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (result.isSuccess) Color(0xFF0A1A0A) else Color(0xFF1A0A0A)
            )
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(if (result.isSuccess) "✅" else "💥", fontSize = 52.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    if (result.isSuccess) "Tamir Başarılı!" else "Usta Elin Kaydı!",
                    color = if (result.isSuccess) MoneyGreen else Color(0xFFFF4444),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (result.isSuccess)
                        "${result.itemName} başarıyla tamir edildi. Değeri arttı!"
                    else
                        "Tamir sırasında bir şeyler ters gitti... ${result.itemName} daha da bozuldu!\nYeni durum: ${result.newCondition}",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (result.isSuccess) MoneyGreen else Color(0xFFFF4444)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (result.isSuccess) "Harika! 🎉" else "Ne yapayım... 😔",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
