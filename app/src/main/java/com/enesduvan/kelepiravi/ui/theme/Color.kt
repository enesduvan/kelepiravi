package com.enesduvan.kelepiravi.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// 1. GENEL DIŞ TEMA (DARK THEME)
// ==========================================
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val Background = Color(0xFF0E1116) // Ana arka plan

val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1B2230), Color(0xFF0A0D12))
)
val Surface = Color(0xFF171C23) // Kart zemini
val SurfaceVariant = Color(0xFF202733) // Hover / secondary yüzey

val PrimaryOrange = Color(0xFFFF7A1A) // Pazarlık Yap butonu
val PrimaryOrangeDark = Color(0xFFE76809) // Pressed state
val PrimaryOrangeSoft = Color(0xFFFFB067) // Badge / accent

val TextPrimary = Color(0xFFF5F7FA) // Ana yazı
val TextSecondary = Color(0xFFA3ADB8) // İkincil yazı
val TextMuted = Color(0xFF6F7782)

val BorderSoft = Color(0xFF2A313D)
val Shadow = Color(0x40000000) // %40 Opaklıkta siyah
val ErrorRed = Color(0xFFFF5252) // Hatalar ve giderler için kırmızı

// --- Condition Badges (Genel) ---
val ConditionPerfectBg = Color(0xFF1E7A39)
val ConditionPerfect = Color(0xFFD7FFE0)

val ConditionScratchBg = Color(0xFF7A6112)
val ConditionScratch = Color(0xFFFFE28A)

val ConditionRepairBg = Color(0xFF8A2D2D)
val ConditionRepair = Color(0xFFFFC5C5)

// --- Top Bar ---
val MoneyGreen = Color(0xFF7CFF9A)
val ReputationGold = Color(0xFFFFC83D)
val LevelBadgeBg = Color(0xFF2B1E12)

// --- Bottom Navigation ---
val NavSelected = Color(0xFFFF7A1A)
val NavUnselected = Color(0xFF8A9099)

val FAB = Color(0xFFFF7A1A)
val FABIcon = Color(0xFFFFFFFF)


// ==========================================
// 2. MARKETPLACE PRODUCT BOTTOM SHEET (YENİ)
// ==========================================

// BACKGROUND
val MarketplaceBackground = Color(0xFF0C1016)
val Overlay = Color(0xB305070C)

val BottomSheet = Color(0xFF171C22)
val BottomSheetTop = Color(0xFF1B2230)

val Card = Color(0xFF151B24)
val CardSecondary = Color(0xFF1A2230)

val MarketBorderSoft = Color(0xFF28313D)
val Divider = Color(0xFF232C36)

val MarketShadow = Color(0x50000000)

// TEXT
val MarketTextPrimary = Color(0xFFF5F7FA)
val MarketTextSecondary = Color(0xFFA4ADB8)
val MarketTextMuted = Color(0xFF727D89)

// PRICE
val PriceText = Color(0xFFFFFFFF)
val EstimatedValue = Color(0xFF45D86E)
val BalanceGreen = Color(0xFF54D548)

// CONDITION (Kırık / Hasarlı Durumu)
val ConditionBrokenBg = Color(0xFFA56B00)
val ConditionBrokenText = Color(0xFFFF8C00)

// DEAL BOX (Ucuzluk/Fırsat Kutusu)
val DealBoxBg = Color(0xFF121B18)
val DealBorder = Color(0xFF284A36)
val DealGreen = Color(0xFF4FE06D)

// INFO CARD (İpucu Paneli)
val TipBg = Color(0xFF201810)
val TipBorder = Color(0xFF9C6820)
val TipIcon = Color(0xFFFFB547)
val TipText = Color(0xFFEBC89A)

// PRIMARY ACTION (Satın Al)
val BuyButton = Color(0xFF54D548)
val BuyButtonPressed = Color(0xFF43C440)
val BuyButtonText = Color(0xFFFFFFFF)

// SECONDARY ACTION (Pazarlık Yap)
val NegotiateBg = Color(0xFF171C22)
val NegotiateBorder = Color(0xFF3DBA3B)
val NegotiateText = Color(0xFF54D548)

// CLOSE BUTTON
val CloseBg = Color(0xFF242B36)
val CloseIcon = Color(0xFFD6DCE5)

// HANDLE (Sayfa Çekme Çubuğu)
val Handle = Color(0xFF5D6673)

// PRODUCT IMAGE
val ImageFrame = Color(0xFF222A36)
val ImageGlow = Color(0x22FF7A1A)