package com.enesduvan.kelepiravi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.enesduvan.kelepiravi.data.local.AppDatabaseProvider
import com.enesduvan.kelepiravi.data.repository.KelepiraviRepository
import com.enesduvan.kelepiravi.ui.market.MarketViewModel
import com.enesduvan.kelepiravi.ui.market.MarketViewModelFactory
import com.enesduvan.kelepiravi.ui.shared.AppRoot
import com.enesduvan.kelepiravi.ui.theme.KelepiraviTheme

class MainActivity : ComponentActivity() {

    /**
     * Manuel DI zinciri:
     * AppDatabaseProvider → KelepiraviDao → KelepiraviRepository → MarketViewModel
     */
    private val viewModel: MarketViewModel by viewModels {
        MarketViewModelFactory(
            repository = KelepiraviRepository(
                database = AppDatabaseProvider.getDatabase(applicationContext)
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KelepiraviTheme {
                AppRoot(viewModel = viewModel)
            }
        }
    }
}
