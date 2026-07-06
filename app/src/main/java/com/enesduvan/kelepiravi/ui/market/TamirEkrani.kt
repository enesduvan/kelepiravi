package com.enesduvan.kelepiravi.ui.market

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enesduvan.kelepiravi.R
import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.ui.shared.formatBalance
import com.enesduvan.kelepiravi.ui.shared.getPainterResourceByName
import com.enesduvan.kelepiravi.ui.shared.EmptyStateIndicator
import com.enesduvan.kelepiravi.ui.shared.marketItemKey
import com.enesduvan.kelepiravi.ui.theme.*

@Composable
fun TamirEkrani(viewModel: MarketViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val repairResult by viewModel.repairResult.collectAsState()
    
    val repairableItems = playerState.inventory.filter { it.condition != "Kusursuz Temiz" }
    val remainingRepairs = viewModel.getRemainingRepairs()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().background(Background).padding(16.dp)
        ) {
            // Başlık
            Text("Tamir Atölyesi", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Eşyaları tamir ederek değerlerini artırabilirsiniz.", color = TextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            // Ch6: Günlük tamir hakkı sayacı
            DailyRepairCounter(remaining = remainingRepairs, total = GameConstants.DAILY_REPAIR_LIMIT)

            Spacer(modifier = Modifier.height(16.dp))
            
            if (repairableItems.isEmpty()) {
                EmptyStateIndicator(
                    iconRes = R.drawable.tamir,
                    title = "Tamirhanede İş Yok",
                    description = "Envanterinde tamir edilecek eşya bulunmuyor."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = repairableItems, key = { marketItemKey(it) }) { item ->
                        RepairItemCard(
                            item = item,
                            viewModel = viewModel,
                            currentBalance = playerState.balance,
                            remainingRepairs = remainingRepairs
                        )
                    }
                }
            }
        }

        // Ch6: Tamir sonucu dialog
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

@Composable
fun RepairItemCard(item: MarketItem, viewModel: MarketViewModel, currentBalance: String, remainingRepairs: Int) {
    val cost = viewModel.calculateRepairCost(item)
    val balanceDouble = currentBalance.toDoubleOrNull() ?: 0.0
    val canAfford = balanceDouble >= cost
    val canRepair = remainingRepairs > 0 && canAfford
    
    val currentVal = item.estimatedValue.toDoubleOrNull() ?: 0.0
    val expectedGain = cost / GameConstants.REPAIR_COST_GAIN_RATE
    val expectedVal = currentVal + expectedGain

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, SurfaceVariant, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = getPainterResourceByName(item.imageName),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(item.itemName, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Durum: ${item.condition}", color = Color(0xFFFFB74D), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Değer: ₺${formatBalance(currentVal.toString())} ➔ ₺${formatBalance(expectedVal.toString())}",
                color = MoneyGreen,
                fontSize = 12.sp
            )
        }
        
        Button(
            onClick = { viewModel.repairItem(item) },
            enabled = canRepair,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryOrange,
                disabledContainerColor = SurfaceVariant
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Tamir",
                    color = if (canRepair) Color.Black else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "₺${formatBalance(cost.toString())}",
                    color = if (canRepair) Color.Black else TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
