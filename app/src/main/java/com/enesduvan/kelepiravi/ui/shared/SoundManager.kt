package com.enesduvan.kelepiravi.ui.shared

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.flow.StateFlow

class SoundManager(private val context: Context, private val isSoundEnabled: StateFlow<Boolean>) {
    private val soundPool: SoundPool
    private var coinSoundId = 0
    private var clickSoundId = 0
    private var applauseSoundId = 0
    private var released = false

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        loadSounds()
    }

    private fun loadSounds() {
        val res = context.resources
        val pkg = context.packageName

        val coinResId = res.getIdentifier("coin", "raw", pkg)
        if (coinResId != 0) coinSoundId = soundPool.load(context, coinResId, 1)

        val clickResId = res.getIdentifier("click", "raw", pkg)
        if (clickResId != 0) clickSoundId = soundPool.load(context, clickResId, 1)

        val applauseResId = res.getIdentifier("applause", "raw", pkg)
        if (applauseResId != 0) applauseSoundId = soundPool.load(context, applauseResId, 1)
    }

    fun playCoinSound() {
        if (released || !isSoundEnabled.value || coinSoundId == 0) return
        soundPool.play(coinSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun playClickSound() {
        if (released || !isSoundEnabled.value || clickSoundId == 0) return
        soundPool.play(clickSoundId, 0.5f, 0.5f, 1, 0, 1f)
    }

    fun playApplauseSound() {
        if (released || !isSoundEnabled.value || applauseSoundId == 0) return
        soundPool.play(applauseSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        if (!released) {
            released = true
            soundPool.release()
        }
    }
}
