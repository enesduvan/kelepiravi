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
import com.enesduvan.kelepiravi.viewmodel.listing.ListingUseCase
import com.enesduvan.kelepiravi.viewmodel.listing.ListingViewModel
import com.enesduvan.kelepiravi.viewmodel.listing.ListingViewModelFactory
import com.enesduvan.kelepiravi.viewmodel.MarketViewModel
import com.enesduvan.kelepiravi.viewmodel.MarketViewModelFactory
import com.enesduvan.kelepiravi.ui.shared.AppRoot
import com.enesduvan.kelepiravi.ui.theme.KelepiraviTheme
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import java.util.concurrent.TimeUnit
import com.enesduvan.kelepiravi.worker.NotificationWorker
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        scheduleOfflineNotifications()

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
