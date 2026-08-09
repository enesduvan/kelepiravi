package com.enesduvan.kelepiravi

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.enesduvan.kelepiravi.data.listing.ListingUseCase
import com.enesduvan.kelepiravi.data.local.SettingsManager
import com.enesduvan.kelepiravi.data.repository.KelepiraviRepository
import com.enesduvan.kelepiravi.database.AppDatabaseProvider
import com.enesduvan.kelepiravi.ui.shared.AppRoot
import com.enesduvan.kelepiravi.ui.shared.SoundManager
import com.enesduvan.kelepiravi.ui.theme.KelepiraviTheme
import com.enesduvan.kelepiravi.viewmodel.MarketViewModel
import com.enesduvan.kelepiravi.viewmodel.MarketViewModelFactory
import com.enesduvan.kelepiravi.viewmodel.bargain.BargainViewModel
import com.enesduvan.kelepiravi.viewmodel.bargain.BargainViewModelFactory
import com.enesduvan.kelepiravi.viewmodel.game.GameViewModel
import com.enesduvan.kelepiravi.viewmodel.game.GameViewModelFactory
import com.enesduvan.kelepiravi.viewmodel.listing.ListingViewModel
import com.enesduvan.kelepiravi.viewmodel.listing.ListingViewModelFactory
import com.enesduvan.kelepiravi.viewmodel.profile.ProfileViewModel
import com.enesduvan.kelepiravi.viewmodel.profile.ProfileViewModelFactory
import com.enesduvan.kelepiravi.viewmodel.repair.RepairViewModel
import com.enesduvan.kelepiravi.viewmodel.repair.RepairViewModelFactory
import com.enesduvan.kelepiravi.worker.NotificationWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val repository by lazy {
        KelepiraviRepository(
            database = AppDatabaseProvider.getDatabase(applicationContext),
            context = applicationContext
        )
    }

    private val settingsManager by lazy { SettingsManager(applicationContext) }
    private val soundManager by lazy {
        SoundManager(
            applicationContext,
            settingsManager.isSoundEnabled
        )
    }

    private val marketViewModel: MarketViewModel by viewModels {
        MarketViewModelFactory(repository, settingsManager, soundManager)
    }

    private val bargainViewModel: BargainViewModel by viewModels {
        BargainViewModelFactory(repository, soundManager)
    }

    private val repairViewModel: RepairViewModel by viewModels {
        RepairViewModelFactory(repository)
    }

    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(repository)
    }

    private val gameViewModel: GameViewModel by viewModels {
        GameViewModelFactory(repository)
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        scheduleOfflineNotifications()

        setContent {
            KelepiraviTheme {
                AppRoot(
                    marketViewModel = marketViewModel,
                    bargainViewModel = bargainViewModel,
                    repairViewModel = repairViewModel,
                    profileViewModel = profileViewModel,
                    gameViewModel = gameViewModel,
                    listingViewModel = listingViewModel
                )
            }
        }
    }

    private fun scheduleOfflineNotifications() {
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(2, TimeUnit.HOURS)
            .build()
            
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "offline_notifications",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
