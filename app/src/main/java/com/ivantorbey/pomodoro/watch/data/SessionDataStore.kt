package com.ivantorbey.pomodoro.watch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore("pomodoro_session")

object PrefKeys {
    val PHASE = stringPreferencesKey("phase")
    val END_TIME_MS = longPreferencesKey("end_time_ms")
    val WORK_DURATION_MIN = intPreferencesKey("work_duration_min")
    val BREAK_DURATION_MIN = intPreferencesKey("break_duration_min")
    val DRAWN_QUESTION = intPreferencesKey("drawn_question")
    val DRAWN_ACTION = intPreferencesKey("drawn_action")
    val DRAWN_LEVER = intPreferencesKey("drawn_lever")
    val CHECKED_MASK = intPreferencesKey("checked_mask")
    val TEST_MODE = booleanPreferencesKey("test_mode")
}

fun Preferences.toSessionState(): SessionState = SessionState(
    phase = try { Phase.valueOf(this[PrefKeys.PHASE] ?: Phase.IDLE.name) } catch (_: Exception) { Phase.IDLE },
    endTimeMs = this[PrefKeys.END_TIME_MS] ?: 0L,
    workDurationMin = this[PrefKeys.WORK_DURATION_MIN] ?: DEFAULT_WORK_MIN,
    breakDurationMin = this[PrefKeys.BREAK_DURATION_MIN] ?: DEFAULT_BREAK_MIN,
    drawnQuestion = this[PrefKeys.DRAWN_QUESTION] ?: -1,
    drawnAction = this[PrefKeys.DRAWN_ACTION] ?: -1,
    drawnLever = this[PrefKeys.DRAWN_LEVER] ?: -1,
    checkedMask = this[PrefKeys.CHECKED_MASK] ?: 0,
    testMode = this[PrefKeys.TEST_MODE] ?: false,
)

fun Context.sessionStateFlow(): Flow<SessionState> =
    sessionDataStore.data.map { it.toSessionState() }

suspend fun Context.readSessionState(): SessionState =
    sessionDataStore.data.first().toSessionState()

suspend fun Context.writeSessionState(state: SessionState) {
    sessionDataStore.edit { prefs ->
        prefs[PrefKeys.PHASE] = state.phase.name
        prefs[PrefKeys.END_TIME_MS] = state.endTimeMs
        prefs[PrefKeys.WORK_DURATION_MIN] = state.workDurationMin
        prefs[PrefKeys.BREAK_DURATION_MIN] = state.breakDurationMin
        prefs[PrefKeys.DRAWN_QUESTION] = state.drawnQuestion
        prefs[PrefKeys.DRAWN_ACTION] = state.drawnAction
        prefs[PrefKeys.DRAWN_LEVER] = state.drawnLever
        prefs[PrefKeys.CHECKED_MASK] = state.checkedMask
        prefs[PrefKeys.TEST_MODE] = state.testMode
    }
}

/** Remet l'état à zéro en conservant les durées préférées et le mode test. */
suspend fun Context.clearSessionState() {
    val current = readSessionState()
    writeSessionState(
        SessionState(
            workDurationMin = current.workDurationMin,
            breakDurationMin = current.breakDurationMin,
            testMode = current.testMode,
        )
    )
}
