package com.enesduvan.kelepiravi.data.market

import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.data.model.MarketItem
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Günlük ekonomi motoru.
 * Her "Yeni Gün" çağrısında envanterdeki ürünlerin fiyatlarını günceller.
 *
 * Tasarım prensipleri:
 * - Elektronik → Yüksek volatilite (teknoloji hızla eskir)
 * - Koleksiyon → Çok yüksek volatilite (antika fiyatları sürpriz yapar)
 * - Mobilya / Ev Aletleri → Düşük volatilite (stabil)
 * - Kondisyon kötüyse negatif bias (bozuk mal daha hızlı değer kaybeder)
 * - Günlük olaylar: %25 ihtimalle tetiklenir, tek kategoriyi veya tüm piyasayı etkiler
 */
object EconomyEngine {

    // ─── Kategori Bazlı Günlük Volatilite (max % salınım) ──────────────────
    private val CATEGORY_VOLATILITY = mapOf(
        "Elektronik"   to 0.13,
        "Koleksiyon"   to 0.20,
        "Mobilya"      to 0.04,
        "Giyim"        to 0.08,
        "Spor"         to 0.09,
        "Ev Aletleri"  to 0.05
    )
    private const val DEFAULT_VOLATILITY = 0.08

    // ─── Kondisyon Bias (negatif → değer kaybetme eğilimi) ─────────────────
    private val CONDITION_BIAS = listOf(
        "Kusursuz" to  0.010,
        "Temiz"    to  0.005,
        "Hafif"    to -0.020,
        "Orta"     to -0.040,
        "Kırık"    to -0.065,
        "Arızalı"  to -0.065,
        "Bantlı"   to -0.085,
        "Tamir"    to -0.085
    )

    // ─── Günlük Olay Havuzu ─────────────────────────────────────────────────
    private val EVENTS = listOf(
        DailyEvent("💥", "Teknoloji Devi İflas Etti!",
            "Sektör güveni sarsıldı — elektronik ürünler değer kaybetti.", "Elektronik", -18.0),
        DailyEvent("🚀", "Teknoloji Fuarı!",
            "Yeni nesil ürünler çıktı — eski modeller değer kazandı.", "Elektronik", +14.0),
        DailyEvent("🏺", "Koleksiyon Fuarı!",
            "Antika ve koleksiyon ürünlerine yoğun ilgi var.", "Koleksiyon", +22.0),
        DailyEvent("📉", "Koleksiyon Balonu Patladı!",
            "Spekülatif fiyatlar çöktü.", "Koleksiyon", -20.0),
        DailyEvent("⚽", "Spor Şampiyonası!",
            "Spor ekipmanlarına ilgi patladı.", "Spor", +15.0),
        DailyEvent("🛋️", "Mobilya Sezonu!",
            "Taşınma sezonu açıldı, mobilya fiyatları yükseldi.", "Mobilya", +10.0),
        DailyEvent("👗", "Moda Haftası!",
            "Butikler dolu — giyim ürünleri değerlendi.", "Giyim", +18.0),
        DailyEvent("🧥", "Kış İndirimleri",
            "Giyim ürünleri büyük indirimde.", "Giyim", -15.0),
        DailyEvent("📊", "Ekonomik Büyüme!",
            "Tüm piyasa genelde pozitif seyretti.", null, +7.0),
        DailyEvent("🌊", "Ekonomik Daralma",
            "Genel piyasa yavaşladı — çoğu ürün değer kaybetti.", null, -9.0),
        DailyEvent("🔥", "Çılgın Talep!",
            "Ev aletlerine yoğun ilgi var.", "Ev Aletleri", +12.0),
        DailyEvent("💸", "Enflasyon Dalgası",
            "Genel fiyatlar yükseldi ama alım gücü düştü.", null, +5.0),
        DailyEvent("❄️", "Talep Dondu",
            "Piyasa çok sakin — satışlar durdu.", null, -6.0),
        DailyEvent("🏆", "Koleksiyoner Rekoru!",
            "Nadir parça rekor fiyata satıldı, piyasa hareketlendi.", "Koleksiyon", +30.0),
    )

    // ─── Ana Fonksiyon ───────────────────────────────────────────────────────

    /**
     * Yeni gün işlemcisi.
     * @return Triple(güncellenmiş envanter, yeni pazar trendleri, tetiklenen olay ya da null)
     */
    fun processNewDay(
        currentDay: Int,
        inventory: List<MarketItem>,
        currentMarketTrends: Map<String, Double>
    ): Triple<List<MarketItem>, Map<String, Double>, DailyEvent?> {
        val rng = Random.Default

        // %25 ihtimalle günlük olay tetiklenir
        val event: DailyEvent? =
            if (rng.nextDouble() < GameConstants.DAILY_EVENT_CHANCE) EVENTS.random(rng) else null

        val updatedInventory = inventory.map { item ->
            val volatility = CATEGORY_VOLATILITY[item.category] ?: DEFAULT_VOLATILITY

            // Kondisyon bias
            val bias = CONDITION_BIAS
                .firstOrNull { (keyword, _) -> item.condition.contains(keyword) }?.second
                ?: GameConstants.DEFAULT_CONDITION_BIAS

            // Temel günlük değişim: rastgele salınım + kondisyon eğilimi
            val baseChange = (rng.nextDouble() * 2.0 - 1.0) * volatility + bias

            // Olay etkisi
            val eventEffect = when {
                event == null -> 0.0
                event.affectedCategory == null -> event.effectPercent / 100.0
                event.affectedCategory == item.category -> event.effectPercent / 100.0
                else -> 0.0
            }

            // Toplam değişim: -35% ile +50% arasında sıkıştırılır
            val totalChange = (baseChange + eventEffect).coerceIn(
                GameConstants.MIN_DAILY_CHANGE,
                GameConstants.MAX_DAILY_CHANGE
            )

            val currentValue = item.estimatedValue.toDoubleOrNull() ?: 100.0
            val originalPurchase = item.purchasePrice.toDoubleOrNull()

            // Yeni değer: orijinal alış fiyatının %10'unun altına düşemez
            val floor = (originalPurchase ?: currentValue) * GameConstants.PURCHASE_VALUE_FLOOR_RATIO
            val newValue = (currentValue * (1.0 + totalChange)).coerceAtLeast(floor)

            // Günlük % değişim (1 ondalık)
            val changePercent = ((totalChange * 1000.0).roundToInt() / 10.0)

            item.copy(
                estimatedValue = newValue.roundToInt().coerceAtLeast(5).toString(),
                dailyChangePercent = changePercent
            )
        }

        // Pazar trendlerini de güncelle
        val newMarketTrends = currentMarketTrends.toMutableMap()
        CATEGORY_VOLATILITY.keys.forEach { category ->
            var currentTrend = newMarketTrends[category] ?: 1.0
            
            // 1. Mean reversion (normale dönüş) - 1.0'a doğru her gün hafifçe yaklaşır
            val diffFromNormal = currentTrend - 1.0
            currentTrend -= diffFromNormal * 0.15 
            
            // 2. Rastgele dalgalanma (±2%)
            val randomDrift = (rng.nextDouble() - 0.5) * 0.04
            currentTrend += randomDrift
            
            // 3. Günlük olay etkisi
            if (event != null && (event.affectedCategory == null || event.affectedCategory == category)) {
                currentTrend += event.effectPercent / 100.0
            }
            
            // Limit the multiplier between 0.5 (çöküş) and 2.0 (balon)
            newMarketTrends[category] = currentTrend.coerceIn(0.5, 2.0)
        }

        return Triple(updatedInventory, newMarketTrends, event)
    }

    /**
     * Toplam portföy değerini hesaplar.
     */
    fun calculatePortfolioValue(inventory: List<MarketItem>): Double =
        inventory.sumOf { it.estimatedValue.toDoubleOrNull() ?: 0.0 }

    /**
     * Toplam yatırılan para.
     */
    fun calculateTotalInvestment(inventory: List<MarketItem>): Double =
        inventory.sumOf { it.purchasePrice.ifEmpty { it.salesValue }.toDoubleOrNull() ?: 0.0 }

    /**
     * Portföy kâr/zarar yüzdesi.....
     */
    fun calculatePortfolioROI(inventory: List<MarketItem>): Double {
        val investment = calculateTotalInvestment(inventory)
        if (investment == 0.0) return 0.0
        val value = calculatePortfolioValue(inventory)
        return ((value - investment) / investment) * 100.0
    }
}
