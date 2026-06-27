package com.enesduvan.kelepiravi.screen

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.enesduvan.kelepiravi.MarketItem
import com.enesduvan.kelepiravi.R
import com.enesduvan.kelepiravi.database.DEFAULT_USER_ID
import com.enesduvan.kelepiravi.database.INITIAL_BALANCE
import com.enesduvan.kelepiravi.database.KelepiraviDatabaseProvider
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

// TEMA RENKLERİ - EKSİKSİZ İÇE AKTARIM
import com.enesduvan.kelepiravi.ui.theme.Background
import com.enesduvan.kelepiravi.ui.theme.BorderSoft
import com.enesduvan.kelepiravi.ui.theme.BottomSheet
import com.enesduvan.kelepiravi.ui.theme.ConditionPerfect
import com.enesduvan.kelepiravi.ui.theme.ConditionPerfectBg
import com.enesduvan.kelepiravi.ui.theme.ConditionRepair
import com.enesduvan.kelepiravi.ui.theme.ConditionRepairBg
import com.enesduvan.kelepiravi.ui.theme.ConditionScratch
import com.enesduvan.kelepiravi.ui.theme.ConditionScratchBg
import com.enesduvan.kelepiravi.ui.theme.Handle
import com.enesduvan.kelepiravi.ui.theme.KelepiraviTheme
import com.enesduvan.kelepiravi.ui.theme.MoneyGreen
import com.enesduvan.kelepiravi.ui.theme.NavSelected
import com.enesduvan.kelepiravi.ui.theme.NavUnselected
import com.enesduvan.kelepiravi.ui.theme.PrimaryOrange
import com.enesduvan.kelepiravi.ui.theme.Surface
import com.enesduvan.kelepiravi.ui.theme.SurfaceVariant
import com.enesduvan.kelepiravi.ui.theme.TextMuted
import com.enesduvan.kelepiravi.ui.theme.TextPrimary
import com.enesduvan.kelepiravi.ui.theme.TextSecondary

private val TurkishLocale: Locale = Locale.forLanguageTag("tr-TR")
private val MarketJson = Json { ignoreUnknownKeys = true }

class HomeScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KelepiraviTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Background,
        bottomBar = {
            NavigationBar(
                containerColor = Surface,
                tonalElevation = 0.dp
            ) {
                AppNavItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = "Pazar",
                    iconRes = R.drawable.home
                )
                AppNavItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = "Envanter",
                    iconRes = R.drawable.envanter
                )
                AppNavItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    label = "Tamir",
                    iconRes = R.drawable.tamir
                )
                AppNavItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    label = "Profil",
                    iconRes = R.drawable.person
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

@Composable
private fun RowScope.AppNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    iconRes: Int
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = FontWeight.Bold) },
        icon = {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = NavSelected,
            selectedTextColor = NavSelected,
            indicatorColor = SurfaceVariant,
            unselectedIconColor = NavUnselected,
            unselectedTextColor = NavUnselected
        )
    )
}

fun loadJsonFromAssets(context: Context, fileName: String): List<MarketItem> {
    return try {
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        Log.d("JSON_OKUMA", "Dosya içeriği: $jsonString")
        MarketJson.decodeFromString<List<MarketItem>>(jsonString)
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

fun formatBalance(balance: String): String {
    val amount = balance.toDoubleOrNull() ?: return balance
    return if (amount % 1.0 == 0.0) {
        String.format(TurkishLocale, "%,.0f", amount)
    } else {
        String.format(TurkishLocale, "%,.2f", amount)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dao = remember(context) {
        KelepiraviDatabaseProvider.getDatabase(context).kelepiraviDao()
    }
    val allInventories by dao.getAllInventories().collectAsState(initial = emptyList())
    val currentUserData = allInventories.firstOrNull { it.id == DEFAULT_USER_ID }
    val balanceText = formatBalance(currentUserData?.balance ?: INITIAL_BALANCE)
    val tamListe = remember { loadJsonFromAssets(context, "json/mock_market.json") }
    var itemList by remember { mutableStateOf(tamListe.shuffled().take(10)) }
    var isRefreshing by remember { mutableStateOf(false) }

    // --- BOTTOM SHEET STATE YAPILARI ---
    var selectedItem by remember { mutableStateOf<MarketItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
                    Text(text = "₺$balanceText", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
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
                        if (!sheetState.isVisible) {
                            selectedItem = null
                        }
                    }
                },
                onPurchaseSuccess = { purchasedItem ->
                    itemList = itemList.filterNot { it == purchasedItem }
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
    val context = LocalContext.current
    val dao = remember(context) {
        KelepiraviDatabaseProvider.getDatabase(context).kelepiraviDao()
    }
    val allInventories by dao.getAllInventories().collectAsState(initial = emptyList())
    val currentUserData = allInventories.firstOrNull { it.id == DEFAULT_USER_ID }
    val inventoryItems = currentUserData?.inventory.orEmpty()
    val totalEstimatedValue = inventoryItems.sumOf { it.estimatedValue.toDoubleOrNull() ?: 0.0 }
    val totalPurchaseValue = inventoryItems.sumOf { it.salesValue.toDoubleOrNull() ?: 0.0 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Envanter",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Satın aldığın fırsatlar burada birikir.",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InventoryStatBox(
                    title = "Ürün",
                    value = inventoryItems.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                InventoryStatBox(
                    title = "Değer",
                    value = "₺${formatBalance(totalEstimatedValue.toString())}",
                    modifier = Modifier.weight(1f)
                )
                InventoryStatBox(
                    title = "Maliyet",
                    value = "₺${formatBalance(totalPurchaseValue.toString())}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (inventoryItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Surface)
                        .border(1.dp, BorderSoft, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.envanter),
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Henüz ürün yok",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pazardan bir ürün alınca burada görünecek.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(items = inventoryItems) { item ->
                InventoryItemCard(item = item)
            }
        }
    }
}

@Composable
private fun InventoryStatBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, BorderSoft, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(text = title, color = TextSecondary, fontSize = 12.sp)
        Text(
            text = value,
            color = MoneyGreen,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}

@Composable
private fun InventoryItemCard(item: MarketItem) {
    val (badgeBg, badgeText) = when {
        item.condition.contains("Kusursuz") || item.condition.contains("Temiz") -> ConditionPerfectBg to ConditionPerfect
        item.condition.contains("Tamir") || item.condition.contains("Bantlı") -> ConditionRepairBg to ConditionRepair
        else -> ConditionScratchBg to ConditionScratch
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = getPainterResourceByName(item.imageName),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = item.itemName,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeBg)
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(text = item.condition, color = badgeText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(text = "Alış: ₺${item.salesValue}", color = TextSecondary, fontSize = 13.sp)
                Text(text = "Tahmini: ₺${item.estimatedValue}", color = MoneyGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(text = "Sat", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(text = "Tamir", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EnvanterEkraniPlaceholder() {
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
