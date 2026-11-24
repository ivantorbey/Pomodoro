package com.ivantorbey.pomodoro.watch.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ivantorbey.pomodoro.watch.PomodoroContent
import com.ivantorbey.pomodoro.watch.data.Phase
import com.ivantorbey.pomodoro.watch.data.readSessionState
import com.ivantorbey.pomodoro.watch.data.writeSessionState
import com.ivantorbey.pomodoro.watch.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmScheduler.ACTION_PHASE_TRANSITION) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handleTransition(context)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun handleTransition(context: Context) {
        val state = context.readSessionState()
        when (state.phase) {
            Phase.WORK -> startBreak(context, state.copy())
            Phase.BREAK -> endBreak(context, state.copy())
            else -> Unit
        }
    }
}

/** Démarre la pause : tirage, persistance, alarme, vibration, notification. */
suspend fun startBreak(context: Context, currentState: com.ivantorbey.pomodoro.watch.data.SessionState) {
    val durationMs = currentState.effectiveBreakDurationMin() * 60_000L
    val endTimeMs = System.currentTimeMillis() + durationMs
    val newState = currentState.copy(
        phase = Phase.BREAK,
        endTimeMs = endTimeMs,
        drawnQuestion = PomodoroContent.QUESTIONS.indices.random(),
        drawnAction = PomodoroContent.ACTIONS.indices.random(),
        drawnLever = PomodoroContent.LEVER.indices.random(),
        checkedMask = 0,
    )
    context.writeSessionState(newState)
    AlarmScheduler.schedulePhaseTransition(context, endTimeMs)
    VibrationHelper.vibrateBreakStart(context)
    NotificationHelper.showBreakStartNotification(context, newState)
}

/** Termine la pause : persistance, vibration, notification. */
private suspend fun endBreak(context: Context, currentState: com.ivantorbey.pomodoro.watch.data.SessionState) {
    val newState = currentState.copy(phase = Phase.BREAK_DONE)
    context.writeSessionState(newState)
    VibrationHelper.vibrateBreakDone(context)
    NotificationHelper.showBreakDoneNotification(context, newState)
}
