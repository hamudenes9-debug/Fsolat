package com.hamudenes.fsolat

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hamudenes.fsolat.notify.PrayerNotifications
import com.hamudenes.fsolat.ui.FsolatApp
import com.hamudenes.fsolat.ui.MainViewModel
import com.hamudenes.fsolat.ui.theme.FsolatTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val locationPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.any { it }) {
                viewModelRef?.onLocationPermissionGranted()
            }
        }

    private var viewModelRef: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PrayerNotifications.ensureChannel(this)

        setContent {
            val vm: MainViewModel = viewModel()
            viewModelRef = vm
            val state by vm.settings.collectAsStateWithLifecycle()

            FsolatTheme(mode = state.themeMode) {
                FsolatApp(
                    viewModel = vm,
                    onRequestNotificationPermission = { requestNotificationPermission() },
                    onRequestLocationPermission = { requestLocationPermission() }
                )
            }
        }

        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun requestLocationPermission() {
        locationPermission.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    override fun attachBaseContext(newBase: android.content.Context) {
        val prefs = newBase.getSharedPreferences("fsolat_locale", MODE_PRIVATE)
        val lang = prefs.getString("lang", "system")
        if (lang == null || lang == "system") {
            super.attachBaseContext(newBase)
            return
        }
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = android.content.res.Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }
}
