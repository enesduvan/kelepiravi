package com.enesduvan.kelepiravi.ui.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enesduvan.kelepiravi.data.model.Listing
import com.enesduvan.kelepiravi.ui.listing.ListingViewModel
import com.enesduvan.kelepiravi.ui.market.MarketViewModel
import com.enesduvan.kelepiravi.ui.shared.EmptyStateIndicator
import com.enesduvan.kelepiravi.ui.shared.formatBalance
import com.enesduvan.kelepiravi.ui.shared.getPainterResourceByName
import com.enesduvan.kelepiravi.ui.theme.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit

@Composable
fun ListingsScreen(marketViewModel: MarketViewModel, listingViewModel: ListingViewModel) {
    val activeListings by listingViewModel.activeListings.collectAsState()
    var lastMinuteBargainOffer by remember { mutableStateOf<Triple<Listing, com.enesduvan.kelepiravi.data.model.Offer, Double>?>(null) }
    var editingListing by remember { mutableStateOf<Listing?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("Aktif İlanlar", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text("Kelepir fırsatlarına gelen teklifler.", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (activeListings.isEmpty()) {
                item {
                    EmptyStateIndicator(
                        iconRes = com.enesduvan.kelepiravi.R.drawable.envanter,
                        title = "Aktif İlanın Yok",
                        description = "Depondaki eşyaları satmak için ilan ver.",
                        modifier = Modifier.height(300.dp)
                    )
                }
            } else {
                items(items = activeListings, key = { it.id }) { listing ->
                    ListingCard(
                        listing = listing,
                        onCancelClick = { listingViewModel.cancelListing(it) },
                        onAcceptOffer = { l, amount -> 
                            if (kotlin.random.Random.nextDouble() < 0.10) {
                                val offer = l.offers.find { it.offerAmount.toDoubleOrNull() == amount }
                                if (offer != null) {
                                    lastMinuteBargainOffer = Triple(l, offer, amount)
                                } else {
                                    listingViewModel.acceptOffer(l, amount)
                                }
                            } else {
                                listingViewModel.acceptOffer(l, amount)
                            }
                        },
                        onBargainClick = { l, offer ->
                            marketViewModel.startSellBargainWithOffer(l.item, offer.npcName, offer.offerAmount.toDouble())
                        },
                        onEditClick = { editingListing = it }
                    )
                }
            }
        }
    }

    if (editingListing != null) {
        var newPriceStr by remember { mutableStateOf(editingListing!!.listedPrice) }
        AlertDialog(
            onDismissRequest = { editingListing = null },
            title = { Text("İlanı Düzenle", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newPriceStr,
                    onValueChange = { if (it.all { char -> char.isDigit() }) newPriceStr = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newPriceStr.isNotBlank()) {
                        listingViewModel.updateListingPrice(editingListing!!, newPriceStr)
                        editingListing = null
                    }
                }) { Text("Güncelle") }
            },
            dismissButton = {
                TextButton(onClick = { editingListing = null }) { Text("İptal", color = TextSecondary) }
            },
            containerColor = Surface
        )
    }

    if (lastMinuteBargainOffer != null) {
        val (l, offer, originalAmount) = lastMinuteBargainOffer!!
        val discount = (originalAmount * 0.05).toLong()
        val newAmount = originalAmount - discount
        AlertDialog(
            onDismissRequest = { lastMinuteBargainOffer = null },
            title = { Text("Son Saniye Pazarlığı!", color = MoneyGreen, fontWeight = FontWeight.Bold) },
            text = { Text("${offer.npcName} son anda fikrini değiştirdi. '₺${formatBalance(newAmount.toString())} olursa hemen alırım' diyor. Ne dersin?", color = TextPrimary) },
            confirmButton = {
                Button(
                    onClick = {
                        listingViewModel.acceptOffer(l, newAmount.toDouble())
                        lastMinuteBargainOffer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen)
                ) { Text("Kabul Et", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { lastMinuteBargainOffer = null }) {
                    Text("Reddet", color = RED)
                }
            },
            containerColor = Surface,
            titleContentColor = MoneyGreen,
            textContentColor = TextPrimary
        )
    }
}

@Composable
fun ListingCard(
    listing: Listing,
    onCancelClick: (Listing) -> Unit,
    onAcceptOffer: (Listing, Double) -> Unit,
    onBargainClick: (Listing, com.enesduvan.kelepiravi.data.model.Offer) -> Unit,
    onEditClick: (Listing) -> Unit
) {
    val estimatedValue = listing.item.estimatedValue.toDoubleOrNull() ?: 0.0
    val listedPrice = listing.listedPrice.toDoubleOrNull() ?: 0.0
    val diffPercent = ((listedPrice - estimatedValue) / estimatedValue) * 100

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = getPainterResourceByName(listing.item.imageName),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(listing.item.itemName, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("İlan Fiyatı: ₺${formatBalance(listing.listedPrice)}", color = PrimaryOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Piyasa: ₺${formatBalance(estimatedValue.toString())} (${if (diffPercent > 0) "+" else ""}${"%.0f".format(diffPercent)}%)", color = TextSecondary, fontSize = 12.sp)
                }
                IconButton(onClick = { onEditClick(listing) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = TextSecondary)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(SurfaceVariant).padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("👀 ${listing.views} Kişi Baktı", color = TextSecondary, fontSize = 12.sp)
                Text("❤️ ${listing.favorites} Favori", color = TextSecondary, fontSize = 12.sp)
                Text("⏳ ${listing.listedDay}. Gün", color = TextSecondary, fontSize = 12.sp)
            }
            
            val comments = remember(listing.id, listing.listedPrice, listing.views) {
                val commentsList = when {
                    diffPercent > 50 -> listOf(
                        "Bu fiyata asla alınmaz!", "Serbest piyasa dedikleri bu mu?", "Kazık!", "Müzeye koysaydın.",
                        "Kardeşim sıfırı daha ucuz bunun.", "Altın kaplama galiba?", "Sen bunu satmak istemiyorsun herhalde.",
                        "Gözüm kanadı fiyata bakınca.", "Bu parayla dükkanı alırım ben."
                    )
                    diffPercent > 20 -> listOf(
                        "Biraz pahalı geldi.", "Fiyatı şişirmiş.", "İndirim yaparsan düşünürüm.", "Pazarlık payı var mı?",
                        "Öğrenciye bir şeyler yapmaz mısın?", "Nakit versem kaça bırakırsın?", "Bence bu para etmez."
                    )
                    diffPercent < -40 -> listOf(
                        "Sudan ucuz!", "Kesin defoludur bu.", "Hemen çök, bedava!", "Şaka gibi fiyat.",
                        "Çalıntı falan değil dimi?", "Adam kafayı yemiş zararına satıyor.", "Bu fırsat kaçmaz, çökelim."
                    )
                    diffPercent < -15 -> listOf(
                        "Fiyatı fena değil.", "Güzel fırsat.", "Uygun yazmışsın.", "Al-sat için ideal.",
                        "Temiz mal, fiyatı da iyi.", "Favoriye attım, yarın alırım.", "Hemen alınır bu."
                    )
                    else -> listOf(
                        "Piyasa fiyatı.", "Normal.", "Makul, düşünülebilir.", "Temiz ürün, ederinde.",
                        "Ortalama fiyat yazmışsın.", "Ne öldürmüş ne diriltmiş, tam ayarında."
                    )
                }
                
                // Yorum sayısı görüntülenme ile yavaş yavaş artsın (Her 15 görüntülenmede 1 yorum, max 5)
                val numComments = (listing.views / 15).coerceIn(1, 5)
                val random = kotlin.random.Random(listing.id.hashCode() + listing.listedPrice.hashCode())
                
                // Aynı yorumun tekrar etmesini önlemek için shuffle ve take kullanalım
                commentsList.shuffled(random).take(numComments)
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                comments.forEach { comment ->
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF1E1E1E)).border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp)).padding(10.dp)
                    ) {
                        Text("💬 \"$comment\"", color = TextMuted, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                }
            }

            if (listing.offers.isEmpty()) {
                Text("Henüz teklif yok. Müşteri bekleniyor...", color = TextMuted, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            } else {
                listing.offers.forEach { offer ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF2A2A2A)).padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(offer.npcName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Teklifi: ₺${formatBalance(offer.offerAmount)}", color = MoneyGreen, fontSize = 13.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onBargainClick(listing, offer) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Pazarlık", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onAcceptOffer(listing, offer.offerAmount.toDouble()) },
                                colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Kabul Et", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { onCancelClick(listing) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, RED),
                modifier = Modifier.fillMaxWidth().height(36.dp)
            ) {
                Text("İlanı Kaldır", color = RED, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
