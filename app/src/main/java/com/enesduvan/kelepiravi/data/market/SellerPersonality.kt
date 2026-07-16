package com.enesduvan.kelepiravi.data.market

import kotlin.math.absoluteValue

// ─── Ch6: Dolandırıcı Tipleri ────────────────────────────────────────────────

enum class ScamType(
    val label: String,
    val warningText: String,
    val revealText: String
) {
    KOZMETIK(
        label = "Kozmetik Dolandırıcı",
        warningText = "Dışarıdan tertemiz görünüyordu...",
        revealText = "İçi tam çürümüş! Ekranda çatlak var, çerçeve eğri. Kazıklandın! 😤"
    ),
    BATARYA(
        label = "Batarya Dolandırıcı",
        warningText = "%100 şarjlı geldi ama...",
        revealText = "Batarya sağlığı %38! Telefon 2 saatte bitiyor. Bu sana pahalıya patlar! 🔋"
    ),
    EKSIK_BILGI(
        label = "Eksik Bilgi Veren",
        warningText = "\"Sorunsuz\" demişti...",
        revealText = "Ekran çatlak, şarj portu bozuk. Adamın 'sorunsuz' sandığı buymuş! 😠"
    ),
    PROFESYONEL(
        label = "Profesyonel Pazarlıkçı",
        warningText = "Sürekli acele ettirmişti...",
        revealText = "Aldatıldın! Ürünü piyasa fiyatından pahalıya sattı ve seni aceleye getirdi. 🎭"
    ),
    SAHTE_KELEPIR(
        label = "Sahte Kelepir",
        warningText = "Fiyat çok iyiydi, kaçırmak istemedim...",
        revealText = "Ürün aslında çok daha değersiz çıktı. Piyasayı iyi araştırmak lazımdı. 📉"
    ),
    KUTU_SATISI(
        label = "Kutu Satıcısı",
        warningText = "Fiyatı çok uygundu...",
        revealText = "Açıklamada 'sadece kutusu satılık' yazıyormuş! Okumadan aldığın için boş kutuya para verdin! 📦😂"
    )
}

// ─── Kişilik Dialogları ───────────────────────────────────────────────────────

data class PersonalityDialogs(
    val buyAccept: List<String>,
    val buyCounter: List<String>,
    val buyLow: List<String>,
    val buyReject: List<String>,
    val sellAccept: List<String>,
    val sellCounter: List<String>,
    val sellHigh: List<String>,
    val sellReject: List<String>,
    // Ch6: Yeni dialog setleri
    val rushPhrases: List<String> = emptyList(),       // Aceleye getirme
    val finalOffer: List<String> = emptyList(),         // "Son fiyat bu"
    val repeatOfferAnnoyed: List<String> = emptyList()  // Aynı teklif tekrar edilince
) {
    fun getBuyAccept() = buyAccept.random()
    fun getBuyCounter() = buyCounter.random()
    fun getBuyLow() = buyLow.random()
    fun getBuyReject() = buyReject.random()
    fun getSellAccept() = sellAccept.random()
    fun getSellCounter() = sellCounter.random()
    fun getSellHigh() = sellHigh.random()
    fun getSellReject() = sellReject.random()
    fun getRushPhrase() = if (rushPhrases.isNotEmpty()) rushPhrases.random() else null
    fun getFinalOffer() = if (finalOffer.isNotEmpty()) finalOffer.random() else null
    fun getRepeatOfferAnnoyed() = if (repeatOfferAnnoyed.isNotEmpty()) repeatOfferAnnoyed.random() else null
}

// ─── Satıcı Kişilikleri ───────────────────────────────────────────────────────

enum class SellerPersonality(
    val title: String,
    val patiencePenaltyMultiplier: Double, // Sabır azalma hızını etkiler
    val buyAcceptRatioModifier: Double,    // Teklif kabul eşiğini değiştirir
    val sellAcceptRatioModifier: Double,   // Alıcı kabul eşiğini değiştirir
    val isScammer: Boolean = false,        // Ch6: Dolandırıcı mı?
    val scamType: ScamType? = null,        // Ch6: Hangi tür dolandırıcı?
    val dialogs: PersonalityDialogs
) {
    NORMAL(
        title = "Normal",
        patiencePenaltyMultiplier = 1.0,
        buyAcceptRatioModifier = 0.0,
        sellAcceptRatioModifier = 0.0,
        dialogs = PersonalityDialogs(
            buyAccept = listOf("Harika! Bu fiyata anlaştık.", "Tamamdır, hayırlı olsun.", "Tamam abi, el sıkışalım."),
            buyCounter = listOf("Biraz daha çıkman lazım.", "Ortasını bulalım.", "Hadi biraz daha ekle.", "Olmaz bu kadarıyla, biraz artır."),
            buyLow = listOf("Çok düşük ya, olmaz.", "O fiyata kurtarmaz.", "Ciddi misin şimdi?"),
            buyReject = listOf("Ölücülük yapma kardeşim, o fiyata vermem.", "Yok öyle bir şey, hadi güle güle."),
            sellAccept = listOf("Harika! Bu fiyata anlaştık.", "Tamam, alıyorum.", "Uygun fiyat, el sıkışalım."),
            sellCounter = listOf("Biraz daha inersen anlaşırız.", "O kadar çıkamam.", "Biraz flexibıl olsan olmaz mı?"),
            sellHigh = listOf("Çok istedin, o kadar etmez.", "Piyasası o kadar değil.", "Sen de abartmışsın bence."),
            sellReject = listOf("Hadi canım sende, piyasası o kadar değil!"),
            finalOffer = listOf("Son fiyat bu, daha inemem.", "Bu son teklifim, al ya da bırak."),
            repeatOfferAnnoyed = listOf("Az önce aynı şeyi söyledin, sağır mıyım ben?", "Tekrar mı? Yok öyle bir şey kardeşim.", "Bu teklifi gördüm zaten, olmaz!")
        )
    ),
    CIMRI(
        title = "Cimri",
        patiencePenaltyMultiplier = 1.5,
        buyAcceptRatioModifier = -0.05,
        sellAcceptRatioModifier = -0.10,
        dialogs = PersonalityDialogs(
            buyAccept = listOf("İçime sinmedi ama peki, al senin olsun.", "Zarar ediyorum ama hadi neyse.", "Pişman olacaksın bu fiyata aldığına."),
            buyCounter = listOf("Şaka mı yapıyorsun? Biraz ciddi bir teklif ver.", "Ben o fiyata kendim alırım!", "Bu ne biçim teklif?"),
            buyLow = listOf("Beni mi soyacaksın? Asla olmaz.", "Bedava vereyim istersen?", "Hayatımda bu kadar gülünç teklif görmedim."),
            buyReject = listOf("Benim malım kıymetlidir, hadi başka kapıya!", "Sen gidersen ben üzülmem, satmıyorum!"),
            sellAccept = listOf("Zorla aldırdın bana, peki.", "İyi, ver bari. Ama ucuza aldın ha.", "Pişmanlıkla kabul ediyorum."),
            sellCounter = listOf("Buna bu kadar para verilmez, in biraz daha.", "Kardeşim param kıymetli, düş fiyatı.", "Tek kuruşum var, abartma."),
            sellHigh = listOf("Altın kaplama mı bu? İn in in.", "Piyasadan haberim var, o fiyat çok.", "Soygunculuk yapıyorsun."),
            sellReject = listOf("Sen beni enayi sandın galiba, hadi işine!", "Bu fiyata almam, defol!"),
            finalOffer = listOf("Son bir kuruş bile artırmam, bu son.", "Buradan daha aşağı gidemem ama bu son fiyatım."),
            repeatOfferAnnoyed = listOf("Kaç kere söyleyeyim? O teklif olmaz!", "Sağır mısın? Hayır dedim!", "Gene mi? Git be kardeşim.")
        )
    ),
    ACELECI(
        title = "Aceleci",
        patiencePenaltyMultiplier = 1.2,
        buyAcceptRatioModifier = 0.10,
        sellAcceptRatioModifier = 0.05,
        dialogs = PersonalityDialogs(
            buyAccept = listOf("Tamamdır uzatmayalım, al hayrını gör.", "Hemen veriyorum, anlaştık.", "Tamam tamam, hızlı ol."),
            buyCounter = listOf("Hadi biraz daha çık hemen vereyim.", "Vaktim yok, ortayı bulalım bitsin bu iş.", "Çabuk ol, bekletme beni."),
            buyLow = listOf("Kurtarmaz o kadar da değil, biraz artır.", "Hızlı satıyoruz dediysek bedava demedik.", "Be adam gibi teklif ver de gidelim."),
            buyReject = listOf("Ohooo seninle uğraşamam, başkasına satarım!", "Vakit kaybetme, git kardeşim."),
            sellAccept = listOf("Hadi tamam, sardım gitti.", "Ver alıyorum, işim gücüm var.", "Tamam tamam hızlı olalım."),
            sellCounter = listOf("Hadi düş biraz daha bitsin bu iş.", "Vaktimi alma, son fiyatını söyle.", "Çabuk çabuk, bekliyorum."),
            sellHigh = listOf("Çok uçtun, biraz mantıklı ol da alalım.", "Acelem var diye kazıklamaya çalışıyorsun?", "İndir şunu, gidiyorum."),
            sellReject = listOf("Seninle vakit kaybedemem, hadi eyvallah!", "Saçma, gidiyorum."),
            rushPhrases = listOf(
                "Az önce biri aradı, 5 dakikaya geliyor!",
                "Başkası da bakıyor bu ürüne, karar ver!",
                "Tren kaçıyor, hızlı ol!",
                "Akşama kadar bekleyemem kardeşim."
            ),
            finalOffer = listOf("Bu son, başka zaman yok.", "Vakit yok, bu son fiyat."),
            repeatOfferAnnoyed = listOf("Dur ya, aynı teklifi mi tekrarlıyorsun?", "Vakit kaybettirme, değişmedi!", "Hızlı düşün, aynı teklif olmaz.")
        )
    ),
    ACEMI(
        title = "Acemi",
        patiencePenaltyMultiplier = 0.5,
        buyAcceptRatioModifier = 0.15,
        sellAcceptRatioModifier = 0.15,
        dialogs = PersonalityDialogs(
            buyAccept = listOf("Aaa gerçekten mi? Tamam anlaştık!", "Peki, sana güveniyorum al bakalım.", "Vay be, çabuk anlaştık!"),
            buyCounter = listOf("Piyasayı çok bilmiyorum ama biraz daha mı etse?", "Arkadaşlarım daha pahalıya satarsın demişti.", "Şimdi ne yapmalıyım bilmiyorum..."),
            buyLow = listOf("O kadar ucuz mu ya? Bilemedim ki...", "Zarar etmeyeyim? Biraz artır bari.", "Hmm bu fiyat doğru mu acaba?"),
            buyReject = listOf("Yok ya, ben bunu satmayacağım vazgeçtim!", "Olmaz, bir daha düşüneyim."),
            sellAccept = listOf("Ooo süper fiyat, hemen alıyorum!", "Vallahi çok iyi denk geldi, anlaştık.", "Harika! Çok iyi fiyat bu!"),
            sellCounter = listOf("Biraz indirim yaparsan alabilirim aslında.", "Öğrenci işi bir fiyat yapsan?", "Çok yüksek, düşemez misin?"),
            sellHigh = listOf("O kadar param yok ki... Biraz daha düşer misin?", "Piyasası o kadarsa ben alamam.", "Vay be bu kadar mı?"),
            sellReject = listOf("Çok pahalıymış ya, ben kaçtım!", "Yok böyle param, olmaz."),
            finalOffer = listOf("Benim için son fiyat bu, çok bilmiyorum ama...", "Bu kadar verebiliyorum, fazlasını bilemiyorum."),
            repeatOfferAnnoyed = listOf("Ee aynı şeyi söyledin, ne yapayım?", "Evet evet biliyorum ama hâlâ düşük...", "Dur, tekrar bak mıydın?")
        )
    ),
    KOLEKSIYONCU(
        title = "Koleksiyoncu",
        patiencePenaltyMultiplier = 0.8,
        buyAcceptRatioModifier = -0.15,
        sellAcceptRatioModifier = 0.20,
        dialogs = PersonalityDialogs(
            buyAccept = listOf("Değerini bilecek birine gidiyor, anlaştık.", "Peki, bu özel parçayı sana bırakıyorum.", "Nadir bu, iyi bak ona."),
            buyCounter = listOf("Bu sıradan bir eşya değil, değerini bil.", "Bunun manevi değeri var, fiyatı artırmalısın.", "Böyle parça pek çıkmaz."),
            buyLow = listOf("Bu teklif bu şahesere hakarettir.", "Gözüm gibi baktım ben buna, o fiyata olmaz.", "Küçümseme bunu."),
            buyReject = listOf("Anlamadığın şeyleri alma sen, satmıyorum!", "Bu fiyata asla. Koleksiyonerler fiyatı bilir."),
            sellAccept = listOf("Kusursuz parça! Ne istersen veririm, anlaştık.", "Koleksiyonum için tam aradığım şey!", "Bunu bulmak zor, alıyorum!"),
            sellCounter = listOf("Kondisyonu fena değil, fiyatı biraz revize edelim.", "Biraz yıpranmış sanki, indirim şart.", "Sertifikalı olmadan tam fiyat vermem."),
            sellHigh = listOf("Nadir olabilir ama o kadar da değil.", "Koleksiyonerim diye abartma fiyatı.", "Piyasayı biliyorum, o kadar etmez."),
            sellReject = listOf("Kondisyonu çok kötü, koleksiyonuma sokmam bunu!", "Hayır, bu standartlarımın altında."),
            finalOffer = listOf("Prensipte inemiyor. Bu son.", "Değerini bilirim, bu son fiyatım."),
            repeatOfferAnnoyed = listOf("Bu teklifi zaten gördüm, değişmedi.", "Tekrarlayarak olmaz kardeşim.", "Prensipim var, aynı teklif kabul edilmez.")
        )
    ),

    // ─── Ch6: Dolandırıcı Kişilikleri ────────────────────────────────────────

    DOLANDIRICI_KOZMETIK(
        title = "Dürüst görünümlü",
        patiencePenaltyMultiplier = 0.3, // Çok sabırlı — kazıklayan aceleci olmaz
        buyAcceptRatioModifier = -0.20,  // Asla ucuza satmaz
        sellAcceptRatioModifier = 0.0,
        isScammer = true,
        scamType = ScamType.KOZMETIK,
        dialogs = PersonalityDialogs(
            buyAccept = listOf(
                "Tertemiz abi, seve seve al! Pişman olmayacaksın.",
                "Bak nasıl sıfır gibi duruyor! Al hayırlı olsun.",
                "Ayna gibi duruyor değil mi? Al gitsin."
            ),
            buyCounter = listOf("O fiyata olmaz, değerini bil bunun.", "Eksiksiz bir ürün bu, anlamayan anlamaz.", "Biraz artır, sıfır mal bu."),
            buyLow = listOf("Sıfır mala bu fiyat olmaz.", "Yeni gibi duruyor, gözün görüyor değil mi?", "Bu ne biçim teklif, ciddiye almıyorum."),
            buyReject = listOf("Hakkını vermeyen almasın, başkasına satarım.", "Değerini bilenler kuyruğa girer buna."),
            sellAccept = listOf("Al bakalım."),
            sellCounter = listOf("İnemem."),
            sellHigh = listOf("Pahalısın."),
            sellReject = listOf("Almıyorum."),
            rushPhrases = listOf(
                "Az önce 2 kişi daha baktı buna, karar ver hızlı.",
                "Bu gece satılmasa markete götürüyorum, oradan çok daha pahalı.",
                "Son bakan oluyor gibi, şanslısın."
            ),
            finalOffer = listOf("Bu son fiyat, vermeyebilirsin ama pişman olursun.", "Son, başkası kapıp gidecek."),
            repeatOfferAnnoyed = listOf("Artık teklif görüşmüyorum bu fiyatta.", "Karar ver, bekletme.")
        )
    ),
    DOLANDIRICI_BATARYA(
        title = "Teknik bilgili",
        patiencePenaltyMultiplier = 0.5,
        buyAcceptRatioModifier = -0.15,
        sellAcceptRatioModifier = 0.0,
        isScammer = true,
        scamType = ScamType.BATARYA,
        dialogs = PersonalityDialogs(
            buyAccept = listOf(
                "Al abi, bak %100 şarjlı, yeni gibi çalışıyor!",
                "Pil sağlığı mükemmel, endişelenme. Al gitsin.",
                "Şarj sorunu yok, bizzat test ettim. Tamam."
            ),
            buyCounter = listOf("Pil sağlığı süper bu telefonun, değer fiyatı.", "Batarya yeni gibi, ucuz da olmaz.", "Teknik olarak mükemmel, biraz artır."),
            buyLow = listOf("Bataryası yeni, o fiyata vermem.", "Bu fiyata teknik bilgin yok demek ki.", "Olmaz, pil performansı mükemmel."),
            buyReject = listOf("Bedavaya mı istiyorsun, git kardeşim.", "Bilmeyene satmam, değerini bilmiyor."),
            sellAccept = listOf("Al."),
            sellCounter = listOf("İnemem."),
            sellHigh = listOf("Pahalısın."),
            sellReject = listOf("Almıyorum."),
            rushPhrases = listOf(
                "Bir tanesi kaldı bu modelden, karar ver.",
                "Servisten çıkardım, gayet sağlam. Hızlı ol.",
                "Eşim bekliyorum, hızlıca kapatalım."
            ),
            finalOffer = listOf("Son fiyat, batarya için endişelenme.", "Bu fiyat yeter, pil sorunsuz."),
            repeatOfferAnnoyed = listOf("Olmaz dedi, anlamıyor musun?", "Teknik değeri var bunun.", "Aynı teklif olmaz.")
        )
    ),
    PROFESYONEL_PAZARLIKCI(
        title = "Piyasa uzmanı",
        patiencePenaltyMultiplier = 0.4,
        buyAcceptRatioModifier = -0.10,
        sellAcceptRatioModifier = 0.0,
        isScammer = true,
        scamType = ScamType.PROFESYONEL,
        dialogs = PersonalityDialogs(
            buyAccept = listOf(
                "Makul fiyata anlaştık, hayırlı olsun.",
                "Piyasayı bilen alır, anlaştık.",
                "İyi yaptın, başka yerde bu fiyata bulamazsın."
            ),
            buyCounter = listOf("Piyasayı iyi araştırdım, bu fiyat yüksek.", "Uzman gözüyle baktım, bu kadar eder.", "Meslekten anlıyorum, değeri bu."),
            buyLow = listOf("Piyasayı bilmiyorsun galiba.", "Uzman değilsen dinle söyleyeni, olmaz.", "O fiyat 2 yıl önceydi."),
            buyReject = listOf("Piyasa araştırmasını yap gel.", "Bilmeden teklif etme."),
            sellAccept = listOf("Tamam."),
            sellCounter = listOf("İn."),
            sellHigh = listOf("Fazla."),
            sellReject = listOf("Olmaz."),
            rushPhrases = listOf(
                "Az önce biri aradı 5 dakikaya geliyor!",
                "Yarın fiyat %20 artıyor, bugün al şansın var.",
                "Başka müşteri bekliyorum arkamda, hızlı karar ver!",
                "Bugünlük son ürün bu, kaçırma!",
                "WhatsApp'tan 3 kişi soruyor, sen düşünürsün onlar alır.",
                "Tren biniyor, in mi binmiyor musun?"
            ),
            finalOffer = listOf(
                "Son fiyat bu, uzman sözü.",
                "Piyasa bilgimle söylüyorum, bu son.",
                "Mesleki olarak son teklifim bu."
            ),
            repeatOfferAnnoyed = listOf("Uzman olarak söylüyorum, o teklif olmaz.", "Piyasayı araştır gel, aynı şeyi tekrarlama.", "Bilgisizce teklif etme.")
        )
    );

    companion object {
        fun fromName(name: String): SellerPersonality {
            val hash = name.hashCode().absoluteValue
            // Ch6: Dolandırıcılar doğrudan ürün üretiminde atanır,
            // isim-tabanlı seçim sadece normal kişilikler için
            val normalPersonalities = entries.filter { !it.isScammer }
            return normalPersonalities[hash % normalPersonalities.size]
        }

        fun getScammerForType(scamType: ScamType): SellerPersonality {
            return entries.firstOrNull { it.scamType == scamType }
                ?: PROFESYONEL_PAZARLIKCI
        }
    }
}
