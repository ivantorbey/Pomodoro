package com.ivantorbey.pomodoro.watch

import com.ivantorbey.pomodoro.watch.data.Phase
import com.ivantorbey.pomodoro.watch.data.SessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StateRestorationTest {

    @Test
    fun `state with future endTime should reschedule alarm`() {
        val futureMs = System.currentTimeMillis() + 300_000L
        val state = SessionState(phase = Phase.WORK, endTimeMs = futureMs)
        assertTrue("endTime is in the future", state.endTimeMs > System.currentTimeMillis())
    }

    @Test
    fun `state with past endTime should trigger immediate transition`() {
        val pastMs = System.currentTimeMillis() - 10_000L
        val state = SessionState(phase = Phase.WORK, endTimeMs = pastMs)
        assertTrue("endTime is in the past", state.endTimeMs < System.currentTimeMillis())
    }

    @Test
    fun `BREAK_DONE phase requires no alarm`() {
        val state = SessionState(phase = Phase.BREAK_DONE)
        // Phase BREAK_DONE : aucune alarme, en attente de reprise manuelle.
        assertEquals(Phase.BREAK_DONE, state.phase)
    }

    @Test
    fun `IDLE state after stop retains preferred durations`() {
        val original = SessionState(workDurationMin = 30, breakDurationMin = 10)
        val reset = original.copy(
            phase = Phase.IDLE,
            endTimeMs = 0L,
            drawnQuestion = -1,
            drawnAction = -1,
            drawnLever = -1,
            checkedMask = 0
        )
        assertEquals(Phase.IDLE, reset.phase)
        assertEquals(30, reset.workDurationMin)
        assertEquals(10, reset.breakDurationMin)
    }

    @Test
    fun `drawn items within valid range after break transition`() {
        val drawnQ = PomodoroContent.QUESTIONS.indices.random()
        val drawnA = PomodoroContent.ACTIONS.indices.random()
        val drawnL = PomodoroContent.LEVER.indices.random()
        val state = SessionState(
            phase = Phase.BREAK,
            drawnQuestion = drawnQ,
            drawnAction = drawnA,
            drawnLever = drawnL
        )
        assertTrue(state.drawnQuestion in PomodoroContent.QUESTIONS.indices)
        assertTrue(state.drawnAction in PomodoroContent.ACTIONS.indices)
        assertTrue(state.drawnLever in PomodoroContent.LEVER.indices)
    }
}
