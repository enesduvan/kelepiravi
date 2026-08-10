package com.enesduvan.kelepiravi.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.enesduvan.kelepiravi.ui.localization.AppLanguage

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("kelepiravi_settings", Context.MODE_PRIVATE)

    // Hızlı Satış (Eski Sistem) Açık mı? Varsayılan: false (Yeni İlan sistemi kullanılacak)
    private val _isFastSellEnabled = MutableStateFlow(prefs.getBoolean(KEY_FAST_SELL, false))
    val isFastSellEnabled: StateFlow<Boolean> = _isFastSellEnabled.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND, true))
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _isHapticEnabled = MutableStateFlow(prefs.getBoolean(KEY_HAPTIC, true))
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING, false))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _language = MutableStateFlow(
        AppLanguage.fromTag(prefs.getString(KEY_LANGUAGE, null) ?: "tr")
    )
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun setFastSellEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FAST_SELL, enabled).apply()
        _isFastSellEnabled.value = enabled
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
        _isSoundEnabled.value = enabled
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
        _isHapticEnabled.value = enabled
    }

    fun setOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING, true).apply()
        _isOnboardingCompleted.value = true
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.tag).apply()
        _language.value = language
    }

    companion object {
        private const val KEY_FAST_SELL = "fast_sell_enabled"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_HAPTIC = "haptic_enabled"
        private const val KEY_ONBOARDING = "onboarding_completed"
        private const val KEY_LANGUAGE = "language"
    }
}
