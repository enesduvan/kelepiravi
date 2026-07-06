package com.enesduvan.kelepiravi.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enesduvan.kelepiravi.ui.theme.PrimaryOrange
import com.enesduvan.kelepiravi.ui.theme.SurfaceVariant
import com.enesduvan.kelepiravi.ui.theme.TextSecondary

@Composable
fun EmptyStateIndicator(
    iconRes: Int,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(SurfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = PrimaryOrange
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Ch6: Animasyonlu bakiye metni.
 * Değer değişince kısa bir renk parlaması (flash) yapar.
 */
@Composable
fun AnimatedBalanceText(
    balance: String,
    fontSize: androidx.compose.ui.unit.TextUnit = 28.sp,
    modifier: Modifier = Modifier
) {
    var previousBalance by remember { mutableStateOf(balance) }
    var isFlashing by remember { mutableStateOf(false) }

    LaunchedEffect(balance) {
        if (previousBalance != balance) {
            isFlashing = true
            kotlinx.coroutines.delay(600)
            isFlashing = false
            previousBalance = balance
        }
    }

    val flashColor by animateColorAsState(
        targetValue = if (isFlashing) {
            val prev = previousBalance.toDoubleOrNull() ?: 0.0
            val curr = balance.toDoubleOrNull() ?: 0.0
            if (curr >= prev) Color(0xFF54D548) else Color(0xFFFF6B6B)
        } else Color.White,
        animationSpec = tween(300),
        label = "balanceFlash"
    )

    Text(
        text = "₺${formatBalance(balance)}",
        color = flashColor,
        fontSize = fontSize,
        fontWeight = FontWeight.ExtraBold,
        modifier = modifier
    )
}
