package com.ivantorbey.pomodoro.watch

import com.ivantorbey.pomodoro.watch.data.Phase
import com.ivantorbey.pomodoro.watch.data.SessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhaseTransitionTest {

    @Test
    fun `initial state is IDLE`() {
        assertEquals(Phase.IDLE, SessionState().phase)
    }

    @Test
    fun `all checked when mask is 0b111`() {
        val state = SessionState(checkedMask = 0b111)
        assertTrue(state.allChecked)
    }

    @Test
    fun `not all checked when mask is partial`() {
        val state = SessionState(checkedMask = 0b011)
        assertFalse(state.allChecked)
    }

    @Test
    fun `unchecked count is 0 when all checked`() {
        assertEquals(0, SessionState(checkedMask = 0b111).uncheckedCount)
    }

    @Test
    fun `unchecked count is 3 when none checked`() {
        assertEquals(3, SessionState(checkedMask = 0).uncheckedCount)
    }

    @Test
    fun `unchecked count is 1 when two checked`() {
        assertEquals(1, SessionState(checkedMask = 0b110).uncheckedCount)
    }

    @Test
    fun `toggle check bit 0 sets question checked`() {
        val mask = 0 xor (1 shl 0)
        assertEquals(1, mask and 1)
    }

    @Test
    fun `toggle check bit 1 sets action checked`() {
        val mask = 0 xor (1 shl 1)
        assertEquals(2, mask and 2)
    }

    @Test
    fun `toggle check bit 2 sets lever checked`() {
        val mask = 0 xor (1 shl 2)
        assertEquals(4, mask and 4)
    }
}
