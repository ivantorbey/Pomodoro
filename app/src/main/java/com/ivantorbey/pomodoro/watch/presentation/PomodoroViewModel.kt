package com.ivantorbey.pomodoro.watch.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ivantorbey.pomodoro.watch.PomodoroContent
import com.ivantorbey.pomodoro.watch.data.Phase
import com.ivantorbey.pomodoro.watch.data.PrefKeys
import com.ivantorbey.pomodoro.watch.data.SessionState
import com.ivantorbey.pomodoro.watch.data.clearSessionState
import com.ivantorbey.pomodoro.watch.data.readSessionState
import com.ivantorbey.pomodoro.watch.data.sessionDataStore
import com.ivantorbey.pomodoro.watch.data.sessionStateFlow
import com.ivantorbey.pomodoro.watch.data.writeSessionState
import com.ivantorbey.pomodoro.watch.notification.NotificationHelper
import com.ivantorbey.pomodoro.watch.timer.AlarmScheduler
import com.ivantorbey.pomodoro.watch.timer.startBreak
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PomodoroViewModel(app: Application) : AndroidViewModel(app) {

    private val ctx = app.applicationContext

    val sessionState: StateFlow<SessionState> = ctx.sessionStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState())

    // ── Actions ──────────────────────────────────────────────────────────────

    fun startSession() = viewModelScope.launch {
        val current = ctx.readSessionState()
        val durationMin = current.effectiveWorkDurationMin()
        val endTimeMs = System.currentTimeMillis() + durationMin * 60_000L
        val newState = current.copy(
            phase = Phase.WORK,
            endTimeMs = endTimeMs,
            drawnQuestion = -1,
            drawnAction = -1,
            drawnLever = -1,
            checkedMask = 0,
        )
        ctx.writeSessionState(newState)
        AlarmScheduler.schedulePhaseTransition(ctx, endTimeMs)
        NotificationHelper.showOngoingNotification(ctx, newState)
    }

    fun pauseNow() = viewModelScope.launch {
        AlarmScheduler.cancelAlarm(ctx)
        startBreak(ctx, ctx.readSessionState())
    }

    fun resumeWork() = viewModelScope.launch {
        val current = ctx.readSessionState()
        val durationMin = current.effectiveWorkDurationMin()
        val endTimeMs = System.currentTimeMillis() + durationMin * 60_000L
        val newState = current.copy(
            phase = Phase.WORK,
            endTimeMs = endTimeMs,
            drawnQuestion = -1,
            drawnAction = -1,
            drawnLever = -1,
            checkedMask = 0,
        )
        ctx.writeSessionState(newState)
        AlarmScheduler.schedulePhaseTransition(ctx, endTimeMs)
        NotificationHelper.showOngoingNotification(ctx, newState)
    }

    fun stopSession() = viewModelScope.launch {
        AlarmScheduler.cancelAlarm(ctx)
        NotificationHelper.cancelAll(ctx)
        ctx.clearSessionState()
    }

    fun toggleCheck(bit: Int) = viewModelScope.launch {
        ctx.sessionDataStore.edit { prefs ->
            val current = prefs[PrefKeys.CHECKED_MASK] ?: 0
            prefs[PrefKeys.CHECKED_MASK] = current xor (1 shl bit)
        }
    }

    fun setWorkDuration(min: Int) = viewModelScope.launch {
        ctx.sessionDataStore.edit { it[PrefKeys.WORK_DURATION_MIN] = min }
    }

    fun setBreakDuration(min: Int) = viewModelScope.launch {
        ctx.sessionDataStore.edit { it[PrefKeys.BREAK_DURATION_MIN] = min }
    }

    fun toggleTestMode() = viewModelScope.launch {
        ctx.sessionDataStore.edit { prefs ->
            prefs[PrefKeys.TEST_MODE] = !(prefs[PrefKeys.TEST_MODE] ?: false)
        }
    }
}
