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
    ,"Mutlu" to "Happy"
    ,"Kararsız" to "Undecided"
    ,"Gergin" to "Tense"
    ,"Kızgın" to "Angry"
    ,"Dost" to "Friend"
    ,"Tanıdık" to "Acquaintance"
    ,"Yabancı" to "Stranger"
    ,"Anlaştınız!" to "Deal agreed!"
    ,"Teklifin (₺)" to "Your Offer (₺)"
    ,"İlan Fiyatı" to "Listing Price"
    ,"Pazarlık Masası" to "Bargaining Table"
    ,"Hoşgeldin Kelepir Avcısı!" to "Welcome, Bargain Hunter!"
    ,"Toplam Kâr" to "Total Profit"
    ,"Net Kâr" to "Net Profit"
    ,"En Büyük Kâr" to "Largest Profit"
    ,"Tek Satışta" to "Single Sale"
    ,"Pazarlık" to "Bargaining"
    ,"Başarı Oranı" to "Success Rate"
    ,"Toplam Satış" to "Total Sales"
    ,"Alınan Ürün" to "Items Bought"
    ,"Favori" to "Favorite"
    ,"Sokak" to "Street"
    ,"Dükkan" to "Shop"
    ,"Galeri" to "Gallery"
    ,"Gecildi" to "Completed"
    ,"Gizemli Görev" to "Mystery Task"
    ,"Nasıl açılacağı bilinmiyor..." to "How to unlock it is unknown..."
    ,"Alış:" to "Purchase:"
    ,"Kusursuz:" to "Perfect:"
    ,"Güncel:" to "Current:"
)

fun localized(text: String, language: AppLanguage): String =
    if (language == AppLanguage.ENGLISH) {
        commonTranslations[text] ?: translateDynamicText(text)
    } else text

private val dynamicTranslations = mapOf(
    "Akıllı Telefon" to "Smartphone",
    "Android Tablet" to "Android Tablet",
    "Oyuncu Laptopu" to "Gaming Laptop",
    "Ofis Laptopu" to "Office Laptop",
    "Oyun Konsolu" to "Game Console",
    "Retro Cep Telefonu" to "Retro Mobile Phone",
    "Kablosuz Kulaklık" to "Wireless Earbuds",
    "Oyuncu Kulaklığı" to "Gaming Headset",
    "Bluetooth Hoparlör" to "Bluetooth Speaker",
    "Mekanik Klavye" to "Mechanical Keyboard",
    "PC Monitör" to "PC Monitor",
    "Mini Projektör" to "Mini Projector",
    "DSLR Fotoğraf Makinesi" to "DSLR Camera",
    "Kahve Makinesi" to "Coffee Maker",
    "Mini Buzdolabı" to "Mini Fridge",
    "Mutfak Blender" to "Kitchen Blender",
    "Filtre Kahve" to "Filter Coffee",
    "Deri Ceket" to "Leather Jacket",
    "Spor Ayakkabı" to "Sneakers",
    "Beyzbol Şapkası" to "Baseball Cap",
    "Kanvas Sırt Çantası" to "Canvas Backpack",
    "Şehir Bisikleti" to "City Bicycle",
    "Dağ Bisikleti" to "Mountain Bike",
    "Elektrikli Scooter" to "Electric Scooter",
    "Akustik Gitar" to "Acoustic Guitar",
    "Klasik Plastik Ördek" to "Classic Rubber Duck",
    "Yumurtadan Çıkan Ördek" to "Hatching Duck",
    "RC Araba" to "RC Car",
    "RC Kamyon" to "RC Truck",
    "Yapboz Bulmaca" to "Jigsaw Puzzle",
    "Renkli Lego Blok" to "Colorful Lego Blocks",
    "Ahşap Satranç Takımı" to "Wooden Chess Set",
    "Araba Lastiği" to "Car Tire",
    "Kol Saati" to "Wristwatch",
    "Gümüş Metal Saat" to "Silver Metal Watch",
    "Akıllı Saat" to "Smartwatch",
    "Hazine Haritası" to "Treasure Map",
    "Kamp Çadırı" to "Camping Tent",
    "Balık Oltası" to "Fishing Rod",
    "Alet Kutusu" to "Toolbox",
    "Katlanır Masa" to "Folding Table",
    "Güneş Gözlüğü" to "Sunglasses",
    "Kırık / Arızalı" to "Broken / Faulty",
    "Hafif Çizik" to "Lightly Scratched",
    "Orta Hasar" to "Moderate Damage",
    "Kusursuz Temiz" to "Perfectly Clean",
    "Kusursuz" to "Perfect",
    "Masrafsız Temiz" to "Clean, No Extra Cost",
    "Lokal Boyalı" to "Locally Painted",
    "Sıfır / Ultra Lüks" to "Brand New / Ultra Luxury",
    "Hatasız Boyasız" to "Flawless, Original Paint",
    "Çırak" to "Apprentice",
    "Ucuz ama riskli" to "Cheap but risky",
    "Usta" to "Expert",
    "Pahalı ama garantili" to "Expensive but guaranteed",
    "Ufak Tadilat" to "Minor Renovation",
    "Boya Badana" to "Painting",
    "Kapsamlı Restorasyon" to "Full Restoration",
    "Sanayi Ustası" to "Auto Shop Mechanic",
    "Lokal Boya" to "Local Paint",
    "Yetkili Servis" to "Authorized Service",
    "Orijinal Parça" to "Original Part",
    "Normal" to "Normal",
    "Cimri" to "Thrifty",
    "Aceleci" to "Impatient",
    "Acemi" to "Beginner",
    "Koleksiyoncu" to "Collector",
    "Dürüst görünümlü" to "Trustworthy-looking",
    "Teknik bilgili" to "Technically Skilled",
    "Piyasa uzmanı" to "Market Expert"
    ,"Siftah Benden" to "First Sale of the Day"
    ,"Ticaretin Kanunu" to "The Law of Trade"
    ,"İşler Tıkırında" to "Business Is Booming"
    ,"Tamirci Çırağı" to "Apprentice Mechanic"
    ,"Usta Eller" to "Skilled Hands"
    ,"Milyoner!" to "Millionaire!"
    ,"Soğuk Su İç" to "Take the Loss"
    ,"Absürt Koleksiyoncu" to "Absurd Collector"
    ,"İlk eşyanı satın aldın. Ticarete hoş geldin!" to "You bought your first item. Welcome to trading!"
    ,"İlk eşyanı sattın. Para akışı başladı!" to "You sold your first item. The money is flowing!"
    ,"Toplam 10 eşya sattın." to "You sold 10 items in total."
    ,"İlk eşyanı tamir ettin. Değer kattın!" to "You repaired your first item. You added value!"
    ,"Toplam 5 eşya tamir ettin." to "You repaired 5 items in total."
    ,"Bakiye 1.000.000₺'ye ulaştı." to "Your balance reached ₺1,000,000."
    ,"Kazıklanarak sahte bir ürün satın aldın." to "You bought a fake item and got scammed."
    ,"NASA Bilgisayarı veya F-16 satın aldın." to "You bought a NASA computer or an F-16."
)

private fun translateDynamicText(text: String): String {
    var translated = text
    dynamicTranslations.entries
        .sortedByDescending { it.key.length }
        .forEach { (turkish, english) -> translated = translated.replace(turkish, english) }
    return translated
}
