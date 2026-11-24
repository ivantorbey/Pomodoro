package com.ivantorbey.pomodoro.watch

import com.ivantorbey.pomodoro.watch.PomodoroContent.ACTIONS
import com.ivantorbey.pomodoro.watch.PomodoroContent.LEVER
import com.ivantorbey.pomodoro.watch.PomodoroContent.QUESTIONS
import com.ivantorbey.pomodoro.watch.PomodoroContent.formatQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawTest {

    @Test
    fun `QUESTIONS list has 35 entries`() {
        assertEquals(35, QUESTIONS.size)
    }

    @Test
    fun `ACTIONS list has 27 entries`() {
        assertEquals(27, ACTIONS.size)
    }

    @Test
    fun `LEVER list has 33 entries`() {
        assertEquals(33, LEVER.size)
    }

    @Test
    fun `random draw is within list bounds`() {
        repeat(100) {
            val q = QUESTIONS.indices.random()
            val a = ACTIONS.indices.random()
            val l = LEVER.indices.random()
            assertTrue(q in QUESTIONS.indices)
            assertTrue(a in ACTIONS.indices)
            assertTrue(l in LEVER.indices)
        }
    }

    @Test
    fun `formatQuestion replaces N with work duration`() {
        val raw = "Amélioration : que puis-je changer dans les {N} prochaines minutes ?"
        val result = formatQuestion(raw, 25)
        assertTrue(result.contains("25"))
        assertFalse(result.contains("{N}"))
    }

    @Test
    fun `formatQuestion with no placeholder is unchanged`() {
        val raw = "Friction : qu'est-ce qui m'a ralenti ou distrait ?"
        val result = formatQuestion(raw, 25)
        assertEquals(raw, result)
    }
}
