package com.enesduvan.kelepiravi.ui.shared

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.enesduvan.kelepiravi.data.model.MarketItem
import kotlinx.serialization.json.Json
import java.util.Locale

private val TurkishLocale: Locale = Locale.forLanguageTag("tr-TR")
private val MarketJson = Json { ignoreUnknownKeys = true }

/**
 * Sayıyı Türk lirası formatında gösterir.
 * Örn: 25000.0 → "25.000", 3600.50 → "3.600,50"
 */
fun formatBalance(balance: String): String {
    val amount = balance.toDoubleOrNull() ?: return balance
    return if (amount % 1.0 == 0.0) {
        String.format(TurkishLocale, "%,.0f", amount)
    } else {
        String.format(TurkishLocale, "%,.2f", amount)
    }
}

/** Assets klasöründeki JSON dosyasından MarketItem listesi okur. */
fun formatBalance(amount: Double): String = formatBalance(amount.toString())

fun marketItemKey(item: MarketItem): String {
    return listOf(
        item.itemName,
        item.sellerName,
        item.purchaseDate,
        item.imageName,
        item.salesValue,
        item.estimatedValue
    ).joinToString("|")
}

fun loadJsonFromAssets(context: Context, fileName: String): List<MarketItem> {
    return try {
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        Log.d("JSON_OKUMA", "Dosya okundu: $fileName")
        MarketJson.decodeFromString<List<MarketItem>>(jsonString)
    } catch (e: Exception) {
        Log.e("JSON_PARSING_HATA", "Dönüştürme hatası: ${e.localizedMessage}", e)
        emptyList()
    }
}

/**
 * Drawable kaynak adını çözümleyerek Painter döndürür.
 * Kaynak bulunamazsa sistem varsayılanını kullanır.
 */
@Composable
fun getPainterResourceByName(name: String): Painter {
    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier(name, "drawable", context.packageName)
    return if (resourceId != 0) {
        painterResource(id = resourceId)
    } else {
        painterResource(id = android.R.drawable.ic_menu_report_image)
    }
}
