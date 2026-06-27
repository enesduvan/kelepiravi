package com.enesduvan.kelepiravi

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

// TEMA RENKLERİ - EKSİKSİZ İÇE AKTARIM
import com.enesduvan.kelepiravi.ui.theme.Background
import com.enesduvan.kelepiravi.ui.theme.BottomSheet
import com.enesduvan.kelepiravi.ui.theme.ConditionPerfect
import com.enesduvan.kelepiravi.ui.theme.ConditionPerfectBg
import com.enesduvan.kelepiravi.ui.theme.ConditionRepair
import com.enesduvan.kelepiravi.ui.theme.ConditionRepairBg
import com.enesduvan.kelepiravi.ui.theme.ConditionScratch
import com.enesduvan.kelepiravi.ui.theme.ConditionScratchBg
import com.enesduvan.kelepiravi.ui.theme.Handle
import com.enesduvan.kelepiravi.ui.theme.KelepiraviTheme
import com.enesduvan.kelepiravi.ui.theme.PrimaryOrange
import com.enesduvan.kelepiravi.ui.theme.Surface
import com.enesduvan.kelepiravi.ui.theme.TextPrimary
import com.enesduvan.kelepiravi.ui.theme.TextSecondary

class HomeScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KelepiraviTheme {
                var selectedTab by remember { mutableStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Background,
                    bottomBar = {
                        NavigationBar(
                            containerColor = Surface,
                            tonalElevation = 0.dp
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                label = { Text("Pazar", fontWeight = FontWeight.Bold) },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.home),
                                        contentDescription = "Pazar"
                                    )
                                }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                label = { Text("Envanter", fontWeight = FontWeight.Bold) },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.envanter),
                                        contentDescription = "Envanter"
                                    )
                                }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                label = { Text("Tamir", fontWeight = FontWeight.Bold) },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.tamir),
                                        contentDescription = "Tamir"
                                    )
                                }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                label = { Text("Profil", fontWeight = FontWeight.Bold) },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.person),
                                        contentDescription = "Profil"
                                    )
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (selectedTab) {
                            0 -> MainScreen()
                            1 -> EnvanterEkrani()
                            2 -> TamirEkrani()
                            3 -> ProfilEkrani()
                        }
                    }
                }
            }
        }
    }
}

fun loadJsonFromAssets(context: Context, fileName: String): List<MarketItem> {
    return try {
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        Log.d("JSON_OKUMA", "Dosya içeriği: $jsonString")
        Json { ignoreUnknownKeys = true }.decodeFromString<List<MarketItem>>(jsonString)
    } catch (e: Exception) {
        Log.e("JSON_PARSING_HATA", "Dönüştürme hatası: ${e.localizedMessage}", e)
        emptyList()
    }
}

@Composable
fun getPainterResourceByName(name: String): Painter {
    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier(name, "drawable", context.packageName)
    return if (resourceId != 0) {
        painterResource(id = resourceId)
    } else {
        painterResource(id = android.R.drawable.ic_menu_report_image)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val tamListe = remember { loadJsonFromAssets(context, "json/mock_market.json") }
    var itemList by remember { mutableStateOf(tamListe.shuffled().take(10)) }
    var isRefreshing by remember { mutableStateOf(false) }

    // --- BOTTOM SHEET STATE YAPILARI ---
    var selectedItem by remember { mutableStateOf<MarketItem?>(null) }
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            itemList = tamListe.shuffled().take(10)
            isRefreshing = false
        },
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.fillMaxSize().background(Background)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Background,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "₺500", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Surface)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "⭐", fontSize = 14.sp)
                            Text(text = "0", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Surface).padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Text(text = "Sokak Satıcısı", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Ayarlar", tint = Color.LightGray, modifier = Modifier.size(24.dp).padding(start = 4.dp))
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(items = itemList) { item ->
                    ItemCard(
                        condition = item.condition,
                        sellerName = item.sellerName,
                        itemName = item.itemName,
                        salesValue = item.salesValue,
                        estimatedValue = item.estimatedValue,
                        image = getPainterResourceByName(item.imageName),
                        onItemClick = {
                            selectedItem = item
                        }
                    )
                }
            }
        }
    }

    // --- DİNAMİK MODAL BOTTOM SHEET TETİKLEYİCİSİ ---
    if (selectedItem != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedItem = null },
            sheetState = sheetState,
            containerColor = BottomSheet,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Handle) }
        ) {
            ProductDetailBottomSheetContent(
                item = selectedItem!!,
                onClose = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) { selectedItem = null }
                    }
                }
            )
        }
    }
}

@Composable
fun ItemCard(
    condition: String,
    sellerName: String,
    itemName: String,
    salesValue: String,
    estimatedValue: String,
    image: Painter,
    onItemClick: () -> Unit
) {
    val (badgeBg, badgeText) = when {
        condition.contains("Kusursuz") || condition.contains("Temiz") -> ConditionPerfectBg to ConditionPerfect
        condition.contains("Tamir") || condition.contains("Bantlı") -> ConditionRepairBg to ConditionRepair
        else -> ConditionScratchBg to ConditionScratch
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        onClick = onItemClick
    ) {
        Row(modifier = Modifier.padding(16.dp).height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.size(110.dp).clip(RoundedCornerShape(20.dp)).background(Color.White), contentAlignment = Alignment.Center) {
                Image(painter = image, contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
            }

            Column(modifier = Modifier.weight(1f).padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = itemName, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(badgeBg).padding(horizontal = 10.dp, vertical = 2.dp)) {
                    Text(text = condition, color = badgeText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Text(text = "Satıcı: $sellerName", color = TextSecondary, fontSize = 14.sp, maxLines = 1)
                Text(text = "Tahmini değer: $estimatedValue₺", color = TextSecondary, fontSize = 14.sp)

                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "₺$salesValue", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)

                    Button(
                        onClick = onItemClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Pazarlık Yap", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(painter = painterResource(id = R.drawable.chat_logo), contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnvanterEkrani() {
    Box(modifier = Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
        Text("Envanter Ekranı Yakında!", color = Color.White, fontSize = 20.sp)
    }
}

@Composable
fun TamirEkrani() {
    Box(modifier = Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
        Text("Tamirhane Yakında!", color = Color.White, fontSize = 20.sp)
    }
}

@Composable
fun ProfilEkrani() {
    Box(modifier = Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
        Text("Profil Ayarları Yakında!", color = Color.White, fontSize = 20.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    KelepiraviTheme {
        MainScreen()
    }
}