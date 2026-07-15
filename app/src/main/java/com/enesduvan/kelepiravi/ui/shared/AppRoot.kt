package com.enesduvan.kelepiravi.ui.shared

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.sp
import com.enesduvan.kelepiravi.R
import com.enesduvan.kelepiravi.ui.inventory.InventoryScreen
import com.enesduvan.kelepiravi.ui.market.BargainScreen
import com.enesduvan.kelepiravi.ui.market.SellBargainScreen
import com.enesduvan.kelepiravi.ui.market.MarketScreen
import com.enesduvan.kelepiravi.ui.market.MarketViewModel
import com.enesduvan.kelepiravi.ui.listing.ListingViewModel
import com.enesduvan.kelepiravi.ui.market.TamirEkrani
import com.enesduvan.kelepiravi.ui.market.ProfilEkrani
import com.enesduvan.kelepiravi.ui.theme.Background
import com.enesduvan.kelepiravi.ui.theme.NavSelected
import com.enesduvan.kelepiravi.ui.theme.NavUnselected
import com.enesduvan.kelepiravi.ui.theme.Surface
import com.enesduvan.kelepiravi.ui.theme.SurfaceVariant
import com.enesduvan.kelepiravi.ui.theme.MoneyGreen
import com.enesduvan.kelepiravi.ui.theme.ReputationGold

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppRoot(marketViewModel: MarketViewModel, listingViewModel: ListingViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val isOnboardingCompleted by listingViewModel.isOnboardingCompleted.collectAsState()
    val bargainState by marketViewModel.bargainState.collectAsState()
    val sellBargainState by marketViewModel.sellBargainState.collectAsState()
    val uiState by marketViewModel.uiState.collectAsState()
    val latestAchievement = uiState.latestAchievement

    Box(modifier = Modifier.fillMaxSize()) {

    // Ch6: Pazarlık ekranlarına geçişte yatay animasyon
    AnimatedContent(
        targetState = Pair(bargainState, sellBargainState),
        contentKey = { (b, s) -> 
            if (b != null) 1 else if (s != null) 2 else 0 
        },
        transitionSpec = {
            val enter = slideInHorizontally(
                animationSpec = tween(350, easing = EaseOutCubic),
                initialOffsetX = { it }
            ) + fadeIn(tween(250))
            val exit = slideOutHorizontally(
                animationSpec = tween(300, easing = EaseInCubic),
                targetOffsetX = { -it / 3 }
            ) + fadeOut(tween(200))
            enter togetherWith exit
        },
        label = "rootTransition"
    ) { (currentBargain, currentSell) ->
        when {
            currentBargain != null -> BargainScreen(viewModel = marketViewModel, bargainState = currentBargain)
            currentSell != null -> SellBargainScreen(viewModel = marketViewModel, sellBargainState = currentSell)
            else -> {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Background,
                    bottomBar = {
                        NavigationBar(
                            containerColor = Surface,
                            tonalElevation = 0.dp
                        ) {
                            NavItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                label = "Pazar",
                                painter = painterResource(id = R.drawable.home)
                            )
                            NavItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                label = "Envanter",
                                painter = painterResource(id = R.drawable.envanter)
                            )
                            NavItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                label = "Tamir",
                                painter = painterResource(id = R.drawable.tamir)
                            )
                            NavItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                label = "Profil",
                                painter = painterResource(id = R.drawable.person)
                            )
                        }
                    }
                ) { innerPadding ->
                    // Ch6: Tab geçişlerinde CrossFade animasyonu
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        Crossfade(
                            targetState = selectedTab,
                            animationSpec = tween(200),
                            label = "tabCrossfade"
                        ) { tab ->
                            when (tab) {
                                0 -> MarketScreen(viewModel = marketViewModel)
                                1 -> InventoryScreen(marketViewModel = marketViewModel, listingViewModel = listingViewModel)
                                2 -> TamirEkrani(viewModel = marketViewModel)
                                3 -> ProfilEkrani(viewModel = marketViewModel, listingViewModel = listingViewModel)
                            }
                        }
                    }
                }
            }
        }
    }

    if (!isOnboardingCompleted) {
        OnboardingDialog(
            onComplete = { listingViewModel.setOnboardingCompleted() }
        )
    }

    // V5.0: Global Başarım Bildirimi (Kupa)
    AnimatedVisibility(
        visible = latestAchievement != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(tween(300)),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(tween(300)),
        modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp, start = 16.dp, end = 16.dp)
    ) {
        val ach = latestAchievement
        if (ach != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).background(SurfaceVariant, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(ach.iconRes, contentDescription = "Kupa", tint = ReputationGold, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("🏆 BAŞARIM AÇILDI!", color = ReputationGold, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(ach.title, color = androidx.compose.ui.graphics.Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(ach.description, color = com.enesduvan.kelepiravi.ui.theme.TextSecondary, fontSize = 12.sp)
                    }
                    if (ach.rewardMoney > 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Ödül", color = MoneyGreen, fontSize = 10.sp)
                            Text("+₺${ach.rewardMoney.toInt()}", color = MoneyGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    } // Box sonu
}

@Composable
private fun RowScope.NavItem(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    painter: Painter
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = FontWeight.Bold) },
        icon = { Icon(painter = painter, contentDescription = label, modifier = Modifier.size(28.dp)) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = NavSelected,
            selectedTextColor = NavSelected,
            indicatorColor = SurfaceVariant,
            unselectedIconColor = NavUnselected,
            unselectedTextColor = NavUnselected
        )
    )
}
