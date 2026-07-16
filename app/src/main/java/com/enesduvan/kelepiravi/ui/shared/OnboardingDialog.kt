package com.enesduvan.kelepiravi.ui.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.enesduvan.kelepiravi.ui.theme.*

@Composable
fun OnboardingDialog(onComplete: () -> Unit) {
    var step by remember { mutableStateOf(1) }

    Dialog(
        onDismissRequest = {}, // Kullanıcı zorla kapatamaz, butonla geçecek
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Background)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val title = when(step) {
                    1 -> "Hoşgeldin Kelepir Avcısı!"
                    2 -> "Değer Mekaniği ve Tamir"
                    else -> "Ticaretin Altın Kuralları"
                }

                Text(
                    text = title,
                    color = PrimaryOrange,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                val content = when(step) {
                    1 -> "Amacın çok basit:\n\nDüşük fiyattan alıp, yüksek fiyata satarak dünyanın en zengin tüccarı olmak!\n\nMarketten fırsatları yakala, envanterine ekle ve kârla sat."
                    2 -> "Her eşyanın iki değeri vardır:\n\n1. Kusursuz Değeri: Eşyanın sıfır/hasarsız halinin piyasa değeridir.\n2. Güncel Değeri: Eşyanın hasar durumuna göre düşmüş gerçek fiyatıdır.\n\nTaktik: Hasarlı malları ucuz fiyattan alıp tamirhaneye götürürsen, eşyanın güncel değeri kusursuz değerine fırlar ve devasa kâr edersin!"
                    else -> "• Pazarlık: Müşteriler sabırsızdır. Çok yüksek fiyat istersen masadan kalkarlar.\n\n• Etkinlikler (Events): Bazen zabıta veya hırsız gelebilir. Verdiğin kararlar itibarını etkiler.\n\n• İtibar: İtibarın yüksekse müşteriler seninle pazarlıkta daha esnek olurlar."
                }

                Text(
                    text = content,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    textAlign = if (step == 1) TextAlign.Center else TextAlign.Start,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    repeat(3) { i ->
                        Box(modifier = Modifier.size(8.dp).background(if (step == i + 1) PrimaryOrange else CardSecondary, RoundedCornerShape(4.dp)))
                        if (i < 2) Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (step < 3) step++ else onComplete()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (step < 3) "Devam Et" else "Oynamaya Başla!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
