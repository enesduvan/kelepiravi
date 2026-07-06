package com.enesduvan.kelepiravi.data.market

import com.enesduvan.kelepiravi.data.model.MarketItem
import kotlin.random.Random
import java.time.LocalDate

enum class LootBoxType(
    val title: String,
    val description: String,
    val price: Double,
    val minItems: Int,
    val maxItems: Int,
    val allowedCategories: List<String>?, // null means all categories
    val rarityBoost: Double // Şans artışı (Legendary çıkma ihtimali için vs)
) {
    SMALL_RETURN_BOX(
        "Ufak İade Kutusu",
        "İçinden çoğunlukla sıradan eşyalar çıkan küçük bir palet.",
        1500.0,
        1,
        3,
        listOf("Mobilya", "Kırtasiye", "Diğer", "Giyim", "Kitap"),
        0.0 // Normal şans
    ),
    TECH_DUMP(
        "Teknoloji Çöplüğü",
        "Sadece Elektronik çıkar. İçinden bozuk telefon da çıkabilir, sağlam bilgisayar da.",
        5000.0,
        2,
        4,
        listOf("Elektronik"),
        0.10 // %10 extra şans
    ),
    LARGE_PALLET(
        "Kapalı İade Paleti",
        "Devasa bir Zamazon paleti. Efsanevi eşya düşürme şansının en yüksek olduğu kutudur.",
        25000.0,
        3,
        7,
        null, // Her şey çıkabilir
        0.25 // %25 extra şans
    )
}

object LootBoxGenerator {
    fun openBox(type: LootBoxType): List<MarketItem> {
        val count = Random.nextInt(type.minItems, type.maxItems + 1)
        val generatedItems = mutableListOf<MarketItem>()

        for (i in 0 until count) {
            // MarketGenerator.generateItems parametresiz kullanılamıyor, rastgele 1 adet alalım
            val baseItems = MarketGenerator.generateItems(10) // Sadece içinden seçeceğiz
            
            val baseItem = if (type.allowedCategories != null) {
                // Kategori uyan bir tane bulana kadar (ya da ilkini al)
                baseItems.firstOrNull { it.category in type.allowedCategories } ?: baseItems.first()
            } else {
                baseItems.first()
            }

            // Kutudan çıkan ürünlerin kondisyonu daha değişkendir
            val conditionRoll = Random.nextDouble()
            val boxCondition = when {
                conditionRoll < 0.2 -> "Kırık / Arızalı"
                conditionRoll < 0.5 -> "Orta Hasar"
                conditionRoll < 0.8 -> "Hafif Çizik"
                else -> "Kusursuz Temiz" 
            }

            // Kutu açıldığında purchasePrice "0" veya tahmini EV üzerinden ayarlanabilir. 
            // Direkt 0 koyalım ki saf kâr olarak hesaplansın. 
            generatedItems.add(
                baseItem.copy(
                    condition = boxCondition,
                    purchasePrice = "0.0", 
                    purchaseDate = LocalDate.now().toString(),
                    sellerName = "Zamazon (${type.title})",
                    isScammer = false
                )
            )
        }
        return generatedItems
    }
}
