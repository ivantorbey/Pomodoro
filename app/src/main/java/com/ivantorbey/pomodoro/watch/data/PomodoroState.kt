package com.ivantorbey.pomodoro.watch.data

const val DEFAULT_WORK_MIN = 25
const val DEFAULT_BREAK_MIN = 5
const val MIN_WORK_MIN = 5
const val MAX_WORK_MIN = 90
const val MIN_BREAK_MIN = 1
const val MAX_BREAK_MIN = 30
const val TEST_DURATION_MIN = 1

enum class Phase { IDLE, WORK, BREAK, BREAK_DONE }

/**
 * État complet d'une session, lu depuis DataStore.
 * Toutes les valeurs ont des défauts pour que la lecture initiale soit valide.
 */
data class SessionState(
    val phase: Phase = Phase.IDLE,
    /** Timestamp système (ms) auquel la phase courante se termine. 0 si IDLE. */
    val endTimeMs: Long = 0L,
    val workDurationMin: Int = DEFAULT_WORK_MIN,
    val breakDurationMin: Int = DEFAULT_BREAK_MIN,
    /** Index dans PomodoroContent.QUESTIONS (-1 si non tiré). */
    val drawnQuestion: Int = -1,
    /** Index dans PomodoroContent.ACTIONS (-1 si non tiré). */
    val drawnAction: Int = -1,
    /** Index dans PomodoroContent.LEVER (-1 si non tiré). */
    val drawnLever: Int = -1,
    /**
     * Bitmask des cases cochées :
     *   bit 0 = question cochée
     *   bit 1 = action cochée
     *   bit 2 = lever cochée
     */
    val checkedMask: Int = 0,
    /** Mode test 60 s / 60 s (non persisté entre sessions). */
    val testMode: Boolean = false,
) {
    val allChecked: Boolean get() = checkedMask and 0b111 == 0b111
    val uncheckedCount: Int get() = Integer.bitCount(0b111 and checkedMask.inv() and 0b111)

    fun effectiveWorkDurationMin(): Int = if (testMode) TEST_DURATION_MIN else workDurationMin
    fun effectiveBreakDurationMin(): Int = if (testMode) TEST_DURATION_MIN else breakDurationMin
}
