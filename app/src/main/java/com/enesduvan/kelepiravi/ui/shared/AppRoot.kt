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
import com.enesduvan.kelepiravi.R
import com.enesduvan.kelepiravi.ui.inventory.InventoryScreen
import com.enesduvan.kelepiravi.ui.market.BargainScreen
import com.enesduvan.kelepiravi.ui.market.SellBargainScreen
import com.enesduvan.kelepiravi.ui.market.MarketScreen
import com.enesduvan.kelepiravi.ui.market.MarketViewModel
import com.enesduvan.kelepiravi.ui.market.TamirEkrani
import com.enesduvan.kelepiravi.ui.market.ProfilEkrani
import com.enesduvan.kelepiravi.ui.theme.Background
import com.enesduvan.kelepiravi.ui.theme.NavSelected
import com.enesduvan.kelepiravi.ui.theme.NavUnselected
import com.enesduvan.kelepiravi.ui.theme.Surface
import com.enesduvan.kelepiravi.ui.theme.SurfaceVariant

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppRoot(viewModel: MarketViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val bargainState by viewModel.bargainState.collectAsState()
    val sellBargainState by viewModel.sellBargainState.collectAsState()

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
            currentBargain != null -> BargainScreen(viewModel = viewModel, bargainState = currentBargain)
            currentSell != null -> SellBargainScreen(viewModel = viewModel, sellBargainState = currentSell)
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
                                0 -> MarketScreen(viewModel = viewModel)
                                1 -> InventoryScreen(viewModel = viewModel)
                                2 -> TamirEkrani(viewModel = viewModel)
                                3 -> ProfilEkrani(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
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
