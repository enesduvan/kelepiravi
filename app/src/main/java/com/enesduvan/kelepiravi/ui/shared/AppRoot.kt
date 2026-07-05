package com.enesduvan.kelepiravi.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.enesduvan.kelepiravi.ui.market.MarketViewModel

@Composable
fun AppRoot(viewModel: MarketViewModel) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Red), contentAlignment = Alignment.Center) {
        Text("HELLO ENES!", fontSize = 48.sp, color = Color.White)
    }
}
