package com.ivantorbey.pomodoro.watch.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ivantorbey.pomodoro.watch.data.Phase
import com.ivantorbey.pomodoro.watch.data.readSessionState
import com.ivantorbey.pomodoro.watch.data.writeSessionState
import com.ivantorbey.pomodoro.watch.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                restoreSession(context)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun restoreSession(context: Context) {
        val state = context.readSessionState()
        when (state.phase) {
            Phase.IDLE -> Unit // rien à faire

            Phase.WORK, Phase.BREAK -> {
                val now = System.currentTimeMillis()
                if (state.endTimeMs > now) {
                    // La phase n'est pas encore terminée : replanifier l'alarme.
                    AlarmScheduler.schedulePhaseTransition(context, state.endTimeMs)
                    NotificationHelper.showOngoingNotification(context, state)
                } else {
                    // L'heure de fin est déjà passée : exécuter la transition immédiatement.
                    when (state.phase) {
                        Phase.WORK -> startBreak(context, state)
                        Phase.BREAK -> {
                            val done = state.copy(phase = Phase.BREAK_DONE)
                            context.writeSessionState(done)
                            VibrationHelper.vibrateBreakDone(context)
                            NotificationHelper.showBreakDoneNotification(context, done)
                        }
                        else -> Unit
                    }
                }
            }

            Phase.BREAK_DONE -> {
                // Toujours en attente de reprise manuelle.
                NotificationHelper.showBreakDoneNotification(context, state)
            }
        }
    }
}
