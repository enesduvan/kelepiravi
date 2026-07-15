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
                Text(
                    text = if (step == 1) "Hoşgeldin Kelepir Avcısı!" else "Ticaretin Kuralları",
                    color = PrimaryOrange,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                if (step == 1) {
                    Text(
                        text = "Amacın çok basit:\n\nDüşük fiyattan alıp, yüksek fiyata satarak dünyanın en zengin antikacısı olmak!",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                } else {
                    Text(
                        text = "1. Müşteriler sabırsızdır, çok fazla teklif yaparsan masadan kalkarlar.\n\n2. Hasarlı eşyaları ucuza alıp tamir ederek dev kârlar elde edebilirsin.\n\n3. Her eşya sana XP ve seviye kazandırır. Haydi başla!",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Start,
                        lineHeight = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (step == 1) step = 2 else onComplete()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (step == 1) "Devam Et" else "Oynamaya Başla!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
