package com.ivantorbey.pomodoro.watch

import com.ivantorbey.pomodoro.watch.data.Phase
import com.ivantorbey.pomodoro.watch.data.SessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerTest {

    @Test
    fun `remaining ms is clamped to zero when endTime is past`() {
        val state = SessionState(
            phase = Phase.WORK,
            endTimeMs = System.currentTimeMillis() - 5_000L
        )
        val remaining = maxOf(0L, state.endTimeMs - System.currentTimeMillis())
        assertEquals(0L, remaining)
    }

    @Test
    fun `remaining ms is positive when endTime is in future`() {
        val futureMs = System.currentTimeMillis() + 60_000L
        val state = SessionState(phase = Phase.WORK, endTimeMs = futureMs)
        val remaining = maxOf(0L, state.endTimeMs - System.currentTimeMillis())
        assertTrue(remaining > 0L)
    }

    @Test
    fun `progress reaches 1f when no time remains`() {
        val totalMs = 25 * 60_000L
        val remainingMs = 0L
        val progress = (1f - remainingMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
        assertEquals(1f, progress, 0.001f)
    }

    @Test
    fun `progress is 0 at session start`() {
        val totalMs = 25 * 60_000L
        val progress = (1f - totalMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
        assertEquals(0f, progress, 0.001f)
    }

    @Test
    fun `test mode uses 1 minute durations`() {
        val state = SessionState(
            workDurationMin = 25,
            breakDurationMin = 5,
            testMode = true
        )
        assertEquals(1, state.effectiveWorkDurationMin())
        assertEquals(1, state.effectiveBreakDurationMin())
    }

    @Test
    fun `normal mode uses configured durations`() {
        val state = SessionState(
            workDurationMin = 30,
            breakDurationMin = 10,
            testMode = false
        )
        assertEquals(30, state.effectiveWorkDurationMin())
        assertEquals(10, state.effectiveBreakDurationMin())
    }
}
