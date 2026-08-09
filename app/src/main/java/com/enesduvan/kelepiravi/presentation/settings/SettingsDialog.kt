package com.enesduvan.kelepiravi.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.enesduvan.kelepiravi.ui.theme.Background
import com.enesduvan.kelepiravi.ui.theme.PrimaryOrange
import com.enesduvan.kelepiravi.ui.theme.Surface
import com.enesduvan.kelepiravi.ui.theme.TextPrimary
import com.enesduvan.kelepiravi.ui.theme.TextSecondary
import com.enesduvan.kelepiravi.ui.localization.AppLanguage
import com.enesduvan.kelepiravi.ui.localization.localized

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    isSoundEnabled: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    isHapticEnabled: Boolean,
    onHapticToggle: (Boolean) -> Unit,
    isQuickSellEnabled: Boolean,
    onQuickSellToggle: (Boolean) -> Unit,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(localized("Ayarlar", "Settings"), color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                SettingToggleRow(
                    title = localized("Ses Efektleri", "Sound Effects"),
                    subtitle = localized("Oyun içi tık, para ve diğer sesler", "In-game clicks, coins and other sounds"),
                    checked = isSoundEnabled,
                    onCheckedChange = onSoundToggle
                )
                
                SettingToggleRow(
                    title = localized("Titreşim (Haptik)", "Haptic Feedback"),
                    subtitle = localized("Dokunuşlarda hafif titreşim hissi", "Light vibration on touch"),
                    checked = isHapticEnabled,
                    onCheckedChange = onHapticToggle
                )

                SettingToggleRow(
                    title = localized("Hızlı Satış Modu", "Quick Sell Mode"),
                    subtitle = localized("Aktif ilan beklemek yerine anında satış", "Sell instantly instead of waiting for a listing"),
                    checked = isQuickSellEnabled,
                    onCheckedChange = onQuickSellToggle
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(localized("Dil", "Language"), color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = language == AppLanguage.TURKISH,
                        onClick = { onLanguageChange(AppLanguage.TURKISH) },
                        label = { Text("Türkçe") }
                    )
                    FilterChip(
                        selected = language == AppLanguage.ENGLISH,
                        onClick = { onLanguageChange(AppLanguage.ENGLISH) },
                        label = { Text("English") }
                    )
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(localized("Kapat", "Close"), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SettingToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PrimaryOrange,
                checkedTrackColor = Surface,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = Background
            )
        )
    }
}
