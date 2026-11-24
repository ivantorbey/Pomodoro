package com.ivantorbey.pomodoro.watch.timer

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object VibrationHelper {

    /** court-court-long : début de pause */
    private val PATTERN_BREAK_START = longArrayOf(0, 150, 100, 150, 100, 600)

    /** long-long : fin de pause */
    private val PATTERN_BREAK_DONE = longArrayOf(0, 500, 200, 500)

    fun vibrateBreakStart(context: Context) = vibrate(context, PATTERN_BREAK_START)
    fun vibrateBreakDone(context: Context) = vibrate(context, PATTERN_BREAK_DONE)

    private fun vibrate(context: Context, pattern: LongArray) {
        if (!canVibrate(context)) return
        val vibrator = getVibrator(context) ?: return
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    /** Respecte Ne pas déranger / mode Coucher : on ne vibre pas si le volume des notifications
     *  est à zéro (mode silencieux). */
    private fun canVibrate(context: Context): Boolean {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audio.ringerMode != AudioManager.RINGER_MODE_SILENT
    }

    private fun getVibrator(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
}
