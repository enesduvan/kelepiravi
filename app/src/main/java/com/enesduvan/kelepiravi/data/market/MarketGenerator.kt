package com.enesduvan.kelepiravi.data.market

import com.enesduvan.kelepiravi.data.model.MarketItem
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Prosedürel pazar üretim motoru.
 * imageName değerleri projedeki gerçek drawable isimleriyle birebir eşleşir.
 */
object MarketGenerator {

    // ─── Mevcut Drawable Havuzu ───────────────────────────────────────────────
    // Kondisyona göre hangi resim seçileceğini belirleyen harita.
    // key = ürün tipi, value = (temiz resimler, hasarlı resimler)
    private data class ImagePool(
        val clean: List<String>,      // Kusursuz / Hafif Çizik için
        val damaged: List<String>     // Orta Hasar / Kırık / Bantlı için
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
            damaged = listOf("laptop_clean_1", "laptop_clean_2")  // Aynı resim, kondisyon badge'i ayrımı sağlar
        ),
        "tablet" to ImagePool(
            clean = listOf("tablet_galaxy"),
            damaged = listOf("tablet_scratched_1", "tablet_scratched_2")
        ),
        "headphones" to ImagePool(
            clean = listOf("headphones_duct_tape_1"),
            damaged = listOf("headphones_duct_tape_1", "headphones_duct_tape_2", "headphones_duct_tape_3")
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

    private data class ProductTemplate(
        val name: String,
        val category: String,
        val imageKey: String,         // IMAGE_POOLS haritasındaki key
        val baseMinValue: Int,
        val baseMaxValue: Int
    )

    private val PRODUCTS = listOf(
        // Elektronik
        ProductTemplate("Akıllı Telefon",         "Elektronik",  "smartphone",    2000, 18000),
        ProductTemplate("Laptop",                  "Elektronik",  "laptop",        4000, 25000),
        ProductTemplate("Tablet",                  "Elektronik",  "tablet",        1500, 12000),
        ProductTemplate("Oyun Kulaklığı",          "Elektronik",  "headphones",     300,  4000),
        ProductTemplate("Oyun Konsolu Kolu",        "Elektronik",  "gamepad",        800,  4000),
        ProductTemplate("Monitör",                 "Elektronik",  "monitor",       2000, 12000),
        ProductTemplate("Mekanik Klavye",           "Elektronik",  "keyboard",       400,  4000),
        ProductTemplate("VR Gözlüğü",             "Elektronik",  "vr",            3000, 15000),

        // Ev Aletleri
        ProductTemplate("Blender",                 "Ev Aletleri", "blender",        200,  1500),
        ProductTemplate("Kahve Makinesi",          "Ev Aletleri", "coffee_maker",   400,  4000),
        ProductTemplate("Mini Buzdolabı",          "Ev Aletleri", "fridge",        1000,  6000),

        // Giyim & Aksesuar
        ProductTemplate("Kol Saati",               "Giyim",       "watch",          500, 15000),
        ProductTemplate("Bez Çanta",               "Giyim",       "backpack",       200,  2000),
        ProductTemplate("Forma",                   "Giyim",       "jersey",         100,  1500),
        ProductTemplate("Deri Ceket",              "Giyim",       "leather_jacket", 400,  5000),

        // Spor
        ProductTemplate("Klasik Bisiklet",         "Spor",        "bicycle",       1500, 10000),
        ProductTemplate("Elektrikli Scooter",      "Spor",        "scooter",       2000, 12000),

        // Koleksiyon & Hobi
        ProductTemplate("Koleksiyon Oyuncağı",     "Koleksiyon",  "toy",             50,  3000),
        ProductTemplate("Akustik Gitar",           "Koleksiyon",  "guitar",         500,  5000),
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

    // ─── Üretim Motoru ────────────────────────────────────────────────────────

    fun generateItems(count: Int = 12): List<MarketItem> = (1..count).map { generateOne() }

    private fun generateOne(): MarketItem {
        val rng = Random.Default
        val product = PRODUCTS.random(rng)
        val condition = CONDITIONS.random(rng)

        // Tahmini değer: baz aralıkta ±%15 varyans
        val baseValue = rng.nextInt(product.baseMinValue, product.baseMaxValue)
        val variance = (baseValue * 0.15 * (rng.nextDouble() - 0.5)).roundToInt()
        val estimatedValue = maxOf(50, baseValue + variance)

        // Piyasa fiyatı: her zaman tahmini değerden ucuz
        val salesRatio = 0.50 + rng.nextDouble() * 0.40
        val salesValue = (estimatedValue * salesRatio * condition.valueMultiplier)
            .roundToInt().coerceAtLeast(30)

        // Kondisyona uygun gerçek drawable seç
        val pool = IMAGE_POOLS[product.imageKey]
        val imageName = pool?.pickFor(condition.label) ?: "smartphone_clean_1"

        return MarketItem(
            condition = condition.label,
            sellerName = SELLERS.random(rng),
            itemName = product.name,
            salesValue = salesValue.toString(),
            estimatedValue = estimatedValue.toString(),
            imageName = imageName,
            category = product.category
        )
    }
}
