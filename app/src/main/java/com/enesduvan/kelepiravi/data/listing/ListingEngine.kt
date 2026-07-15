package com.enesduvan.kelepiravi.data.listing

import com.enesduvan.kelepiravi.data.model.Listing
import com.enesduvan.kelepiravi.data.model.Offer
import kotlin.math.max
import kotlin.random.Random

object ListingEngine {

    /**
     * Gün sonu tetiklenir. Aktif ilanların istatistiklerini artırır ve teklif üretir.
     */
    fun processDay(activeListings: List<Listing>): List<Listing> {
        return activeListings.map { listing ->
            val estimatedValue = listing.item.estimatedValue.toDoubleOrNull() ?: 1.0
            val listedPrice = listing.listedPrice.toDoubleOrNull() ?: 1.0
            
            // diffRatio = İstenen fiyatın gerçek fiyata oranı.
            // 1.0 = tam değerinde, 0.8 = %20 ucuza kelepir, 1.5 = %50 pahalı kazık
            val diffRatio = listedPrice / estimatedValue 
            
            // 1. Görüntülenme (Views) hesaplama
            val baseViews = Random.nextInt(5, 25)
            // Ucuza koyduysa daha çok kişi bakar
            val viewsMultiplier = max(0.1, 2.0 - diffRatio) 
            val newViews = listing.views + (baseViews * viewsMultiplier).toInt()
            
            // 2. Favoriler (Favorites)
            // Görüntüleyenlerin %10-20'si favoriye alır
            val newFavorites = listing.favorites + ((newViews - listing.views) * Random.nextDouble(0.1, 0.2)).toInt()

            // 3. Teklif (Offer) Üretme
            val newOffers = listing.offers.toMutableList()
            
            // Fiyata göre teklif gelme ihtimali
            val offerChance = when {
                diffRatio <= 0.8 -> 0.85 // %85 ihtimal (Kelepir)
                diffRatio <= 1.0 -> 0.50 // %50 ihtimal (Değerinde)
                diffRatio <= 1.2 -> 0.20 // %20 ihtimal (Pahalı)
                else -> 0.05             // %5 ihtimal (Çok Pahalı)
            }
            
            if (Random.nextDouble() < offerChance) {
                // NPC'ler genellikle piyasa değerinin altında veya ilan fiyatına yakın teklif verir
                val npcType = getRandomNpcType()
                val offerAmount = calculateOfferAmount(estimatedValue, listedPrice, npcType)
                
                newOffers.add(
                    Offer(
                        npcName = npcType.nameLabel,
                        offerAmount = offerAmount.toLong().toString()
                    )
                )
            }
            
            listing.copy(
                listedDay = listing.listedDay + 1, // İlanda geçen gün sayısı
                views = newViews,
                favorites = newFavorites,
                offers = newOffers
            )
        }
    }

    /**
     * Gerçek zamanlı tetiklenir (örn: 2-3 saniyede bir). İlan ekranındayken anlık etkileşim verir.
     */
    fun processTick(listing: Listing): Listing {
        val estimatedValue = listing.item.estimatedValue.toDoubleOrNull() ?: 1.0
        val listedPrice = listing.listedPrice.toDoubleOrNull() ?: 1.0
        val diffRatio = listedPrice / estimatedValue
        
        // Görüntülenme
        val baseViews = Random.nextInt(1, 5)
        val viewsMultiplier = max(0.1, 2.0 - diffRatio)
        val newViews = listing.views + (baseViews * viewsMultiplier).toInt()
        
        // Favori
        var newFavorites = listing.favorites
        if (Random.nextDouble() < 0.1 * viewsMultiplier) {
            newFavorites += 1
        }
        
        // Teklif (Tick başına ihtimal)
        val newOffers = listing.offers.toMutableList()
        val offerChance = when {
            diffRatio <= 0.8 -> 0.15  // Her tickte %15
            diffRatio <= 1.0 -> 0.05  // Her tickte %5
            diffRatio <= 1.2 -> 0.02  // Her tickte %2
            else -> 0.005             // Kazık fiyat %0.5
        }
        
        if (Random.nextDouble() < offerChance) {
            val isVip = Random.nextDouble() < 0.05 && listing.item.condition.contains("Kusursuz")
            if (isVip) {
                newOffers.add(Offer(npcName = "VIP Müşteri", offerAmount = (listedPrice * Random.nextDouble(1.1, 1.3)).toLong().toString()))
            } else {
                val npcType = getRandomNpcType()
                val offerAmount = calculateOfferAmount(estimatedValue, listedPrice, npcType)
                newOffers.add(Offer(npcName = npcType.nameLabel, offerAmount = offerAmount.toLong().toString()))
            }
        }
        
        return listing.copy(
            views = newViews,
            favorites = newFavorites,
            offers = newOffers
        )
    }
    
    private fun calculateOfferAmount(estimatedValue: Double, listedPrice: Double, npcType: NpcCustomerType): Double {
        return when (npcType) {
            NpcCustomerType.PAZARLIKCI -> estimatedValue * Random.nextDouble(0.7, 0.85) // Çok ölücü
            NpcCustomerType.OGRENCI -> estimatedValue * Random.nextDouble(0.8, 0.95)    // Bütçesi dar
            NpcCustomerType.NORMAL -> estimatedValue * Random.nextDouble(0.9, 1.05)     // Mantıklı
            NpcCustomerType.KOLEKSIYONCU -> listedPrice * Random.nextDouble(0.95, 1.1)  // İlan fiyatını verir, hatta bazen fazla verir
        }
    }
    
    private fun getRandomNpcType(): NpcCustomerType {
        val roll = Random.nextDouble()
        return when {
            roll < 0.3 -> NpcCustomerType.PAZARLIKCI
            roll < 0.6 -> NpcCustomerType.NORMAL
            roll < 0.85 -> NpcCustomerType.OGRENCI
            else -> NpcCustomerType.KOLEKSIYONCU
        }
    }

    enum class NpcCustomerType(val nameLabel: String) {
        PAZARLIKCI("Ölücü Burak"),
        NORMAL("Dürüst Esnaf Ali"),
        OGRENCI("Öğrenci Kardeşimiz"),
        KOLEKSIYONCU("Koleksiyoncu Cem")
    }
}
