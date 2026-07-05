package com.enesduvan.kelepiravi.ui.market

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enesduvan.kelepiravi.R
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.ui.shared.formatBalance
import com.enesduvan.kelepiravi.ui.shared.getPainterResourceByName
import com.enesduvan.kelepiravi.ui.shared.EmptyStateIndicator
import com.enesduvan.kelepiravi.ui.theme.*

@Composable
fun TamirEkrani(viewModel: MarketViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    
    val repairableItems = playerState.inventory.filter { it.condition != "Kusursuz Temiz" }

    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(16.dp)
    ) {
        Text("Tamir Atölyesi", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Eşyaları tamir ederek değerlerini artırabilirsiniz.", color = TextSecondary, fontSize = 14.sp)
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
                items(repairableItems) { item ->
                    RepairItemCard(item, viewModel, playerState.balance)
                }
            }
        }
    }
}

@Composable
fun RepairItemCard(item: MarketItem, viewModel: MarketViewModel, currentBalance: String) {
    val cost = viewModel.calculateRepairCost(item)
    val balanceDouble = currentBalance.toDoubleOrNull() ?: 0.0
    val canAfford = balanceDouble >= cost
    
    val currentVal = item.estimatedValue.toDoubleOrNull() ?: 0.0
    val expectedGain = (cost / 0.60) // based on our formula gain * 0.60 = cost
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
            Text("Değer: ₺${formatBalance(currentVal.toString())} ➔ ₺${formatBalance(expectedVal.toString())}", color = MoneyGreen, fontSize = 12.sp)
        }
        
        Button(
            onClick = { viewModel.repairItem(item) },
            enabled = canAfford,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryOrange,
                disabledContainerColor = SurfaceVariant
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Tamir\n₺${formatBalance(cost.toString())}", color = if (canAfford) Color.Black else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
