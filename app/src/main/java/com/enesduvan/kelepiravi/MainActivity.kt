package com.enesduvan.kelepiravi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.enesduvan.kelepiravi.data.local.AppDatabaseProvider
import com.enesduvan.kelepiravi.data.local.SettingsManager
import com.enesduvan.kelepiravi.data.repository.KelepiraviRepository
import com.enesduvan.kelepiravi.ui.listing.ListingUseCase
import com.enesduvan.kelepiravi.ui.listing.ListingViewModel
import com.enesduvan.kelepiravi.ui.listing.ListingViewModelFactory
import com.enesduvan.kelepiravi.ui.market.MarketViewModel
import com.enesduvan.kelepiravi.ui.market.MarketViewModelFactory
import com.enesduvan.kelepiravi.ui.shared.AppRoot
import com.enesduvan.kelepiravi.ui.theme.KelepiraviTheme

class MainActivity : ComponentActivity() {

    private val repository by lazy {
        KelepiraviRepository(
            database = AppDatabaseProvider.getDatabase(applicationContext),
            context = applicationContext
        )
    }

    private val settingsManager by lazy { SettingsManager(applicationContext) }
    private val soundManager by lazy { com.enesduvan.kelepiravi.data.local.SoundManager(applicationContext, settingsManager.isSoundEnabled) }

    private val marketViewModel: MarketViewModel by viewModels {
        MarketViewModelFactory(repository, settingsManager, soundManager)
    }

    private val listingViewModel: ListingViewModel by viewModels {
        ListingViewModelFactory(
            listingUseCase = ListingUseCase(repository),
            settingsManager = settingsManager
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KelepiraviTheme {
                AppRoot(marketViewModel = marketViewModel, listingViewModel = listingViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
