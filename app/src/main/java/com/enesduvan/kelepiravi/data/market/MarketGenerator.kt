package com.enesduvan.kelepiravi.data.market

import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.data.model.MarketItem
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Prosedürel pazar üretim motoru.
 * imageName değerleri projedeki gerçek drawable isimleriyle birebir eşleşir.
 */
object MarketGenerator {

    // ─── Mevcut Drawable Havuzu ───────────────────────────────────────────────
    private data class ImagePool(
        val clean: List<String>,
        val damaged: List<String>
    ) {
        fun pickFor(condition: String): String {
            val rng = Random.Default
            return when {
                condition.contains("Kusursuz") || condition.contains("Temiz") || condition.contains("Hafif") ->
                    if (clean.isNotEmpty()) clean.random(rng) else (damaged + clean).random(rng)
                else ->
                    if (damaged.isNotEmpty()) damaged.random(rng) else clean.random(rng)
            }
        }
    }

    private val IMAGE_POOLS = mapOf(
        "smartphone" to ImagePool(
            clean = listOf(
                "smartphone_clean_1", "smartphone_clean_2", "smartphone_clean_3",
                "smartphone_clean_4", "smartphone_clean_5", "smartphone_clean_6",
                "smartphone_clean_7", "smartphone_clean_8", "smartphone_clean_9",
                "smartphone_clean_10", "smartphone_clean_11", "smartphone_clean_12",
                "smartphone_scratched_1", "smartphone_dirty_1", "smartphone_dirty_2"
            ),
            damaged = listOf(
                "smartphone_cracked_1", "smartphone_cracked_2", "smartphone_cracked_3",
                "smartphone_cracked_4", "smartphone_cracked_5", "smartphone_cracked_6",
                "smartphone_cracked_7", "smartphone_cracked_8",
                "smartphone_dirty_3", "smartphone_dirty_4", "smartphone_melted_back"
            )
        ),
        "laptop" to ImagePool(
            clean = listOf("laptop_clean_1", "laptop_clean_2"),
            damaged = listOf("laptop_clean_1", "laptop_clean_2")
        ),
        "tablet" to ImagePool(
            clean = listOf("tablet_galaxy"),
            damaged = listOf("tablet_scratched_1", "tablet_scratched_2")
        ),
        "headphones" to ImagePool(
            clean = listOf("oyun_kulakligi"),
            damaged = listOf("oyun_kulakligi")
        ),
        "camera" to ImagePool(
            clean = listOf("kamera_resim"),
            damaged = listOf("kamera_resim")
        ),
        "nasa_bilgisayari" to ImagePool(
            clean = listOf("nasa_bilgisayari"),
            damaged = listOf("nasa_bilgisayari")
        ),
        "satilik_kaynana" to ImagePool(
            clean = listOf("satilik_kaynana"),
            damaged = listOf("satilik_kaynana")
        ),
        "koy_kahvesi" to ImagePool(
            clean = listOf("koy_kahvesi"),
            damaged = listOf("koy_kahvesi")
        ),
        "f_16" to ImagePool(
            clean = listOf("f_16"),
            damaged = listOf("f_16")
        ),
        "oven" to ImagePool(
            clean = listOf("mini_firin"),
            damaged = listOf("mini_firin")
        ),
        "backpack" to ImagePool(
            clean = listOf("bez_canta"),
            damaged = listOf("bez_canta")
        ),
        "bogaz_koprusu" to ImagePool(
            clean = listOf("bogaz_koprusu"),
            damaged = listOf("bogaz_koprusu")
        ),
        "gamepad" to ImagePool(
            clean = listOf("gamepad_broken_1"),
            damaged = listOf("gamepad_broken_1", "gamepad_broken_2")
        ),
        "monitor" to ImagePool(
            clean = listOf("monitor_cracked_1"),
            damaged = listOf("monitor_cracked_1", "monitor_cracked_2", "monitor_cracked_3")
        ),
        "keyboard" to ImagePool(
            clean = listOf("mech_keyboard_clean_1"),
            damaged = listOf("mech_keyboard_missing_key_1")
        ),
        "blender" to ImagePool(
            clean = listOf("blender_1", "blender_2", "blender_3", "blender_4"),
            damaged = listOf("blender_1", "blender_2")
        ),
        "coffee_maker" to ImagePool(
            clean = listOf("coffee_maker_1", "coffee_maker_2", "coffee_maker_3", "coffee_maker_4"),
            damaged = listOf("coffee_maker_1", "coffee_maker_4")
        ),
        "fridge" to ImagePool(
            clean = listOf("mini_fridge_1", "mini_fridge_2"),
            damaged = listOf("mini_fridge_1", "mini_fridge_2")
        ),
        "watch" to ImagePool(
            clean = listOf("classic_watch_1", "classic_watch_2"),
            damaged = listOf("classic_watch_1", "classic_watch_2")
        ),
        "backpack" to ImagePool(
            clean = listOf("canvas_backpack_1", "canvas_backpack_2"),
            damaged = listOf("canvas_backpack_1", "canvas_backpack_2")
        ),
        "bicycle" to ImagePool(
            clean = listOf("classic_bicycle_1"),
            damaged = listOf("classic_bicycle_1")
        ),
        "scooter" to ImagePool(
            clean = listOf("electric_scooter_1"),
            damaged = listOf("electric_scooter_1")
        ),
        "jersey" to ImagePool(
            clean = listOf("dirty_jersey_1"),
            damaged = listOf("dirty_jersey_1", "dirty_jersey_2")
        ),
        "leather_jacket" to ImagePool(
            clean = listOf("leather_jacket_clean_1"),
            damaged = listOf("leather_jacket_torn_1")
        ),
        "toy" to ImagePool(
            clean = listOf("rubber_duck_1", "rubber_duck_2", "rubber_duck_3",
                           "rubber_duck_4", "rubber_duck_5", "rubber_duck_6", "rubber_duck_7"),
            damaged = listOf("rubber_duck_egg_1", "rubber_duck_egg_2", "ufo_toy_1", "ufo_toy_2")
        ),
        "guitar" to ImagePool(
            clean = listOf("acoustic_guitar_clean_1"),
            damaged = listOf("acoustic_guitar_broken_string_1")
        ),
        "vr" to ImagePool(
            clean = listOf("vr_headset_clean_1"),
            damaged = listOf("vr_headset_scratched_1")
        )
    )

    // ─── Ürün Şablonları ──────────────────────────────────────────────────────

    data class ProductTemplate(
        val name: String,
        val category: String,
        val imageKey: String,
        val baseMinValue: Int,
        val baseMaxValue: Int
    )

    val PRODUCTS = listOf(
        ProductTemplate("Akıllı Telefon",         "Elektronik",  "smartphone",    2000, 18000),
        ProductTemplate("Laptop",                  "Elektronik",  "laptop",        4000, 25000),
        ProductTemplate("Gaming Laptop",           "Elektronik",  "laptop",        8000, 35000),
        ProductTemplate("Tablet",                  "Elektronik",  "tablet",        1500, 12000),
        ProductTemplate("Oyun Kulaklığı",          "Elektronik",  "headphones",     300,  4000),
        ProductTemplate("Oyun Konsolu Kolu",        "Elektronik",  "gamepad",        800,  4000),
        ProductTemplate("Oyun Konsolu",            "Elektronik",  "console",       1500, 10000),
        ProductTemplate("Monitör",                 "Elektronik",  "monitor",       2000, 12000),
        ProductTemplate("Mekanik Klavye",           "Elektronik",  "keyboard",       400,  4000),
        ProductTemplate("VR Gözlüğü",             "Elektronik",  "vr",            3000, 15000),
        ProductTemplate("Kamera",                  "Elektronik",  "camera",        1000, 8000),

        // Ev Aletleri
        ProductTemplate("Blender",                 "Ev Aletleri", "blender",        200,  1500),
        ProductTemplate("Kahve Makinesi",          "Ev Aletleri", "coffee_maker",   400,  4000),
        ProductTemplate("Mini Buzdolabı",          "Ev Aletleri", "fridge",        1000,  6000),
        ProductTemplate("Mini Fırın",              "Ev Aletleri", "oven",           300,  2000),

        // Giyim & Aksesuar
        ProductTemplate("Kol Saati",               "Giyim",       "watch",          500, 15000),
        ProductTemplate("Antika Köstekli Saat",    "Giyim",       "watch",         5000, 20000),
        ProductTemplate("Bez Çanta",               "Giyim",       "backpack",       200,  2000),
        ProductTemplate("Forma",                   "Giyim",       "jersey",         100,  1500),
        ProductTemplate("Deri Ceket",              "Giyim",       "leather_jacket", 400,  5000),

        // Spor
        ProductTemplate("Klasik Bisiklet",         "Spor",        "bicycle",       1500, 10000),
        ProductTemplate("Elektrikli Scooter",      "Spor",        "scooter",       2000, 12000),

        // Koleksiyon & Hobi
        ProductTemplate("Koleksiyon Oyuncağı",     "Koleksiyon",  "toy",             50,  3000),
        ProductTemplate("Akustik Gitar",           "Koleksiyon",  "guitar",         500,  5000),

        // Absürt İlanlar (Nadir)
        ProductTemplate("Boğaz Köprüsü (Hissedar)", "Emlak", "bogaz_koprusu", 5000000, 15000000),
        ProductTemplate("NASA Bilgisayarı", "Elektronik", "nasa_bilgisayari", 1000000, 5000000),
        ProductTemplate("F-16 (Anahtarı Kayıp)", "Araç", "f_16", 20000000, 50000000),
        ProductTemplate("Satılık Kaynana", "Diğer", "satilik_kaynana", 10, 100),
        ProductTemplate("Köy Kahvesi", "Emlak", "koy_kahvesi", 100000, 500000)
    )

    // ─── Kondisyon Havuzu (Ağırlıklı) ─────────────────────────────────────────

    private data class Condition(val label: String, val valueMultiplier: Double)

    private val CONDITIONS = buildList {
        repeat(20) { add(Condition("Kusursuz Temiz",         1.00)) }
        repeat(30) { add(Condition("Hafif Çizik",            0.82)) }
        repeat(25) { add(Condition("Orta Hasar",             0.65)) }
        repeat(15) { add(Condition("Kırık / Arızalı",        0.40)) }
        repeat(10) { add(Condition("Bantlı / Tamir Gerekli", 0.25)) }
    }

    // Dolandırıcıların sahte olarak gösterdiği kondisyon (her zaman "Kusursuz")
    private val SCAMMER_DISPLAYED_CONDITIONS = listOf("Kusursuz Temiz")

    // Dolandırıcıdan alındıktan sonra ortaya çıkacak gerçek kondisyon havuzu
    private val SCAMMER_HIDDEN_CONDITIONS = buildList {
        repeat(3)  { add(Condition("Orta Hasar",             0.65)) }
        repeat(5)  { add(Condition("Kırık / Arızalı",        0.40)) }
        repeat(2)  { add(Condition("Bantlı / Tamir Gerekli", 0.25)) }
    }

    // ─── Satıcı Havuzu ────────────────────────────────────────────────────────

    private val SELLERS = listOf(
        "Sabırsız Murat", "Pazarcı Hüseyin", "Eski Çarşı Ali", "Hesaplı Fatma",
        "Sürekli İndirim Yaşar", "Acele Satan Kemal", "Güvenilir Mehmet",
        "Kapı Kapı Dolaşan Necip", "Fırsatçı Selin", "Temiz Mal Derya",
        "İkinci El Emre", "Stok Eritme Ahmet", "Son Fiyat Leyla",
        "Hızlı Kazan Pınar", "Uygun Fiyat Osman", "Seri Satan Zeynep",
        "Taze Mal Tarık", "Şanslı Gün Ayşe", "Net Fiyat Barış",
        "Temiz Ev Nazan", "Anlık Fiyat Serkan", "Güler Yüzlü Gülden",
        "Eşyacı Tuncay", "Bol Stok Ferhat", "Dürüst Satıcı Sedef",
        "Komşu Pazarı İsmail", "Anlık İndirim Handan", "Kaliteli Mal Cemal",
        "Eski Dost Ufuk", "Fırsat Kaçmaz Dilek", "Takas Yapan Tolga",
        "Elde Kalan Sibel", "Stok Fazlası Bülent", "İkinci Şans Burak",
        "Son Fiyat Esen", "Hızlı İşlem Korhan", "Her Şey Satılık Özge"
    )

    // Dolandırıcı isimleri — masum görünümlü
    private val SCAMMER_SELLERS = listOf(
        "Güvenilir Halit", "Dürüst Semih", "Temiz Adam Kürşat",
        "Açık Kalpli Nuri", "Şeffaf Satıcı Vedat", "Doğru Sözlü Cengiz",
        "Namuslu Hakan", "Helal Süt Emmiş Tarcan", "Nezaketli Faruk"
    )

    fun getRandomName(): String = SELLERS.random(Random.Default)

    // ─── Üretim Motoru ────────────────────────────────────────────────────────

    fun getConditionMultiplier(name: String): Double {
        return CONDITIONS.find { it.label == name }?.valueMultiplier
            ?: GameConstants.PERFECT_CONDITION_MULTIPLIER
    }

    fun generateItems(
        count: Int = GameConstants.MARKET_BATCH_SIZE,
        marketTrends: Map<String, Double> = emptyMap()
    ): List<MarketItem> =
        (1..count).map { generateOne(marketTrends) }

    private fun generateOne(marketTrends: Map<String, Double>): MarketItem {
        val rng = Random.Default
        val product = PRODUCTS.random(rng)
        val isScammer = rng.nextDouble() < GameConstants.SCAMMER_CHANCE

        return if (isScammer) {
            generateScammerItem(rng, product, marketTrends)
        } else {
            generateNormalItem(rng, product, marketTrends)
        }
    }

    fun generateNormalItem(
        rng: Random,
        product: ProductTemplate,
        marketTrends: Map<String, Double>
    ): MarketItem {
        val condition = CONDITIONS.random(rng)
        val baseValue = rng.nextInt(product.baseMinValue, product.baseMaxValue)
        val variance = (
            baseValue *
                GameConstants.MARKET_VALUE_VARIANCE_RATE *
                (rng.nextDouble() - 0.5)
            ).roundToInt()
        val trendMultiplier = marketTrends[product.category] ?: 1.0
        
        val extras = mutableListOf<String>()
        var extraMultiplier = 1.0
        
        if (rng.nextDouble() < 0.20) {
            extras.add("Faturalı & Garantili")
            extraMultiplier += 0.10
        }
        if (rng.nextDouble() < 0.15 && product.category == "Elektronik") {
            extras.add("Kutusu Açılmamış")
            extraMultiplier += 0.15
        } else if (rng.nextDouble() < 0.10 && product.category == "Elektronik") {
            extras.add("Şarj Aleti Eksik")
            extraMultiplier -= 0.05
        }

        val estimatedValue = maxOf(GameConstants.MARKET_MIN_ITEM_VALUE, ((baseValue + variance) * trendMultiplier * extraMultiplier).roundToInt())

        val salesRatio = GameConstants.MARKET_MIN_SALES_RATIO +
            rng.nextDouble() * GameConstants.MARKET_SALES_RATIO_RANGE
        val salesValue = (estimatedValue * salesRatio * condition.valueMultiplier)
            .roundToInt().coerceAtLeast(GameConstants.MARKET_MIN_SALES_VALUE)

        val pool = IMAGE_POOLS[product.imageKey]
        val imageName = pool?.pickFor(condition.label) ?: "smartphone_clean_1"

        return MarketItem(
            condition = condition.label,
            sellerName = SELLERS.random(rng),
            itemName = product.name,
            salesValue = salesValue.toString(),
            estimatedValue = estimatedValue.toString(),
            imageName = imageName,
            category = product.category,
            extras = extras
        )
    }

    private fun generateScammerItem(
        rng: Random,
        product: ProductTemplate,
        marketTrends: Map<String, Double>
    ): MarketItem {
        // Dolandırıcı tipi seç
        val scamType = ScamType.entries.random(rng)

        // Gerçek kondisyon (gizli)
        val hiddenCondition = SCAMMER_HIDDEN_CONDITIONS.random(rng)

        // Gösterilen kondisyon: her zaman "Kusursuz Temiz"
        val displayedCondition = SCAMMER_DISPLAYED_CONDITIONS.random(rng)

        // Gerçek değer: hiddenCondition multiplier ile
        val baseValue = rng.nextInt(product.baseMinValue, product.baseMaxValue)
        val trendMultiplier = marketTrends[product.category] ?: 1.0
        val trueEstimatedValue = maxOf(
            GameConstants.MARKET_MIN_ITEM_VALUE,
            (baseValue * trendMultiplier * hiddenCondition.valueMultiplier).roundToInt()
        )

        // Dolandırıcı satış fiyatı: Sahte kelepir → gerçek değerden fazla
        // Diğerleri → kusursuz değermiş gibi satıyor (kazık)
        val fakePerfectValue = maxOf(
            GameConstants.MARKET_MIN_ITEM_VALUE,
            (baseValue * trendMultiplier).roundToInt()
        )

        val salesValue = when (scamType) {
            ScamType.SAHTE_KELEPIR -> {
                // Piyasadan pahalı ama "ucuz" gibi gösterir
                (fakePerfectValue * (1.10 + rng.nextDouble() * 0.20)).roundToInt()
            }
            else -> {
                // Bozuk malı sağlam gibi gösterip tam fiyat istiyor
                val ratio = GameConstants.MARKET_MIN_SALES_RATIO +
                    rng.nextDouble() * GameConstants.MARKET_SALES_RATIO_RANGE * 0.5
                (fakePerfectValue * ratio).roundToInt()
            }
        }

        // Görsel: temiz görsel (dolandırıcı aldatıyor)
        val pool = IMAGE_POOLS[product.imageKey]
        val imageName = pool?.pickFor("Kusursuz") ?: "smartphone_clean_1"

        // Dolandırıcı isim havuzundan seç
        val sellerName = SCAMMER_SELLERS.random(rng)

        return MarketItem(
            condition = displayedCondition,
            sellerName = sellerName,
            itemName = product.name,
            salesValue = salesValue.toString(),
            estimatedValue = fakePerfectValue.toString(), // Sahte tahmini değer
            imageName = imageName,
            category = product.category,
            isScammer = true,
            scamType = scamType.name,
            hiddenCondition = hiddenCondition.label
        )
    }
}
