package com.enesduvan.kelepiravi.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage(val tag: String) {
    TURKISH("tr"),
    ENGLISH("en");

    companion object {
        fun fromTag(tag: String?): AppLanguage =
            if (tag?.startsWith("en", ignoreCase = true) == true) ENGLISH else TURKISH
    }
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.TURKISH }

@Composable
fun localized(turkish: String, english: String): String =
    if (LocalAppLanguage.current == AppLanguage.ENGLISH) english else turkish

@Composable
fun localized(text: String): String = localized(text, LocalAppLanguage.current)

private val commonTranslations = mapOf(
    "Pazar" to "Market",
    "Envanter" to "Inventory",
    "İlanlar" to "Listings",
    "Tamir" to "Repair",
    "Profil" to "Profile",
    "Ayarlar" to "Settings",
    "Ses Efektleri" to "Sound Effects",
    "Titreşim (Haptik)" to "Haptic Feedback",
    "Hızlı Satış Modu" to "Quick Sell Mode",
    "Dil" to "Language",
    "Kapat" to "Close",
    "Satın Al" to "Buy",
    "Pazarlık Yap" to "Bargain",
    "Satışa Koy" to "List for Sale",
    "İlan Ver" to "Create Listing",
    "İlanı Düzenle" to "Edit Listing",
    "İlanı Kaldır" to "Remove Listing",
    "İptal" to "Cancel",
    "Kabul Et" to "Accept",
    "Reddet" to "Reject",
    "Geri" to "Back",
    "İleri" to "Next",
    "Tamam" to "Done",
    "Yok" to "None",
    "Mevcut" to "Available",
    "Kilitli" to "Locked",
    "Kazanıldı" to "Earned",
    "Ürün" to "Item",
    "Adet" to "Quantity",
    "Fiyat" to "Price",
    "Bakiye" to "Balance",
    "Değer" to "Value",
    "Kategori" to "Category",
    "Açıklama:" to "Description:",
    "Satıcı" to "Seller",
    "Alıcı Modu" to "Buyer Mode",
    "Satıcı Modu" to "Seller Mode",
    "Teklif Gönder" to "Send Offer",
    "Teklifi Kabul Et" to "Accept Offer",
    "Kendi teklifini gir..." to "Enter your offer...",
    "Tamir Atölyesi" to "Repair Workshop",
    "Tamirhane" to "Repair Shop",
    "Tamire Başla" to "Start Repair",
    "Tamir Ediliyor..." to "Repairing...",
    "Tamir Başarılı!" to "Repair Successful!",
    "İş Yok" to "No Jobs",
    "Başarı" to "Success",
    "Ödül" to "Reward",
    "Kupa" to "Achievement",
    "İpucu" to "Tip",
    "Tümü" to "All",
    "Ara" to "Search",
    "Temizle" to "Clear",
    "Oynamaya Başla!" to "Start Playing!",
    "Günlük Tamir Hakkı" to "Daily Repair Limit",
    "Elektronik" to "Electronics",
    "Ev Aletleri" to "Home Appliances",
    "Giyim" to "Clothing",
    "Spor" to "Sports",
    "Koleksiyon" to "Collectibles",
    "Sokak Satıcısı" to "Street Trader",
    "Ne arıyorsunuz?" to "What are you looking for?",
    "Yeni Gün" to "New Day",
    "Yeni" to "New",
    "Kusursuz" to "Perfect",
    "Hasarlı" to "Damaged",
    "Kırık / Arızalı" to "Broken / Faulty",
    "Orta Hasar" to "Moderate Damage",
    "Hafif Çizik" to "Lightly Scratched",
    "Kusursuz Temiz" to "Perfectly Clean",
    "İstenen" to "Asking",
    "Puan" to "Rating",
    "Satış" to "Sales",
    "Katılım" to "Joined",
    "Bu Satıcının Diğer İlanları" to "Other Listings by This Seller",
    "Devam Et" to "Continue",
    "Palet Açıldı!" to "Pallet Opened!",
    "Devam etmek için ekrana dokun" to "Tap the screen to continue",
    "Durum:" to "Condition:",
    "Değer:" to "Value:",
    "İlan Fiyatı:" to "Listing Price:",
    "Teklifi:" to "Offer:",
    "Favori" to "Favorite",
    "Kişi Baktı" to "Views",
    "Gün" to "Day",
    "Henüz teklif yok. Müşteri bekleniyor..." to "No offers yet. Waiting for a buyer..."
)

fun localized(text: String, language: AppLanguage): String =
    if (language == AppLanguage.ENGLISH) commonTranslations[text] ?: text else text
