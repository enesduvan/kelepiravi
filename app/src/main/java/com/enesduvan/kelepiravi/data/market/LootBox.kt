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
    val allowedCategories: List<String>?, // null means all normal categories
    val rarityBoost: Double
) {
    SMALL_RETURN_BOX(
        "Ufak İade Kutusu",
        "İçinden çoğunlukla sıradan ev eşyası, giyim ve oyuncaklar çıkan küçük paket.",
        1500.0,
        1,
        3,
        listOf("Home_appliances", "Clothing", "Toys", "Hobby", "Accessories", "Spare_parts", "Sports"),
        0.0
    ),
    TECH_DUMP(
        "Teknoloji Çöplüğü",
        "Sadece Elektronik çıkar. İçinden bozuk kulaklık da çıkabilir, oyun laptopu da.",
        5000.0,
        2,
        4,
        listOf("Electronics"),
        0.10
    ),
    LARGE_PALLET(
        "Kapalı İade Paleti",
        "Devasa bir Zamazon paleti. Çeşit çeşit sıradan ve değerli ürünlerin çıktığı büyük palet.",
        25000.0,
        3,
        7,
        null, // Her normal kategori çıkabilir
        0.25
    )
}

object LootBoxGenerator {
    fun openBox(type: LootBoxType): List<MarketItem> {
        val rng = Random.Default
        val count = rng.nextInt(type.minItems, type.maxItems + 1)
        val generatedItems = mutableListOf<MarketItem>()

        // Sadece NORMAL_PRODUCTS arasından seç (Absürt / Epik eşyalar kesinlikle çıkmaz)
        val eligibleProducts = if (type.allowedCategories != null) {
            MarketGenerator.NORMAL_PRODUCTS.filter { product ->
                type.allowedCategories.any { allowed ->
                    product.category.equals(allowed, ignoreCase = true) ||
                    (allowed.equals("Elektronik", ignoreCase = true) && product.category.equals("Electronics", ignoreCase = true)) ||
                    (allowed.equals("Giyim", ignoreCase = true) && product.category.equals("Clothing", ignoreCase = true)) ||
                    (allowed.equals("Mobilya", ignoreCase = true) && product.category.equals("Home_appliances", ignoreCase = true))
                }
            }
        } else {
            MarketGenerator.NORMAL_PRODUCTS
        }.ifEmpty { MarketGenerator.NORMAL_PRODUCTS }

        for (i in 0 until count) {
            val selectedProduct = eligibleProducts.random(rng)
            val baseItem = MarketGenerator.generateNormalItem(rng, selectedProduct, emptyMap())

            val conditionRoll = rng.nextDouble()
            val boxCondition = when {
                conditionRoll < 0.2 -> "Kırık / Arızalı"
                conditionRoll < 0.5 -> "Orta Hasar"
                conditionRoll < 0.8 -> "Hafif Çizik"
                else -> "Kusursuz Temiz" 
            }

            generatedItems.add(
                baseItem.copy(
                    condition = boxCondition,
                    purchasePrice = 0L, 
                    purchaseDate = LocalDate.now().toString(),
                    sellerName = "Zamazon (${type.title})",
                    isScammer = false
                )
            )
        }
        return generatedItems
    }
}
