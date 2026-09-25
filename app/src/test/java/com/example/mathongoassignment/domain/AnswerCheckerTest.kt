package com.example.mathongoassignment.domain

import com.example.mathongoassignment.domain.answer.Answer
import com.example.mathongoassignment.domain.answer.AnswerChecker
import com.example.mathongoassignment.domain.model.QuestionType
import com.example.mathongoassignment.options
import com.example.mathongoassignment.question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerCheckerTest {

    // --- singleCorrect: the required scope ---

    @Test
    fun `single correct is correct when the flagged option is selected`() {
        val question = question(options = options(false, true, false, false))

        val result = AnswerChecker.check(question, Answer(selectedOptionIds = setOf("opt1")))

        assertTrue(result.isCorrect)
        assertEquals(setOf("opt1"), result.correctOptionIds)
    }

    @Test
    fun `single correct is wrong when another option is selected`() {
        val question = question(options = options(false, true, false, false))

        val result = AnswerChecker.check(question, Answer(selectedOptionIds = setOf("opt2")))

        assertFalse(result.isCorrect)
        // The correct option is still reported so the screen can highlight it.
        assertEquals(setOf("opt1"), result.correctOptionIds)
    }

    @Test
    fun `single correct is wrong with no selection`() {
        val question = question(options = options(true, false))

        assertFalse(AnswerChecker.check(question, Answer(selectedOptionIds = emptySet())).isCorrect)
    }

    @Test
    fun `single correct is wrong when more than one option is selected`() {
        val question = question(options = options(true, false, false))

        val result = AnswerChecker.check(question, Answer(selectedOptionIds = setOf("opt0", "opt1")))

        assertFalse(result.isCorrect)
    }

    @Test
    fun `single correct does not crash when the data flags no correct option`() {
        val question = question(options = options(false, false))

        val result = AnswerChecker.check(question, Answer(selectedOptionIds = setOf("opt0")))

        assertFalse(result.isCorrect)
        assertTrue(result.correctOptionIds.isEmpty())
    }

    // --- multipleCorrect bonus ---

    @Test
    fun `multiple correct needs the exact complete set`() {
        val question = question(
            type = QuestionType.MULTIPLE_CORRECT,
            options = options(true, true, false, false),
        )

        assertTrue(AnswerChecker.check(question, Answer(selectedOptionIds = setOf("opt0", "opt1"))).isCorrect)
        assertFalse(AnswerChecker.check(question, Answer(selectedOptionIds = setOf("opt0"))).isCorrect)
        assertFalse(
            AnswerChecker.check(question, Answer(selectedOptionIds = setOf("opt0", "opt1", "opt2"))).isCorrect,
        )
    }

    // --- numerical bonus ---

    @Test
    fun `numerical accepts any value inside the inclusive range`() {
        val question = question(
            type = QuestionType.NUMERICAL,
            lowerLimit = 2.0,
            upperLimit = 2.5,
        )

        assertTrue(AnswerChecker.check(question, Answer(numericInput = "2")).isCorrect)
        assertTrue(AnswerChecker.check(question, Answer(numericInput = "2.5")).isCorrect)
        assertTrue(AnswerChecker.check(question, Answer(numericInput = " 2.25 ")).isCorrect)
        assertFalse(AnswerChecker.check(question, Answer(numericInput = "2.51")).isCorrect)
    }

    @Test
    fun `numerical falls back to the correct value when no range is given`() {
        val question = question(
            type = QuestionType.NUMERICAL,
            correctValue = 9.3,
            correctValueLabel = "9.3",
        )

        assertTrue(AnswerChecker.check(question, Answer(numericInput = "9.3")).isCorrect)
        assertFalse(AnswerChecker.check(question, Answer(numericInput = "9.31")).isCorrect)
        assertEquals("9.3", AnswerChecker.check(question, Answer(numericInput = "9.3")).correctAnswerLabel)
    }

    @Test
    fun `numerical rejects input that is not a number`() {
        val question = question(type = QuestionType.NUMERICAL, correctValue = 4.0)

        assertFalse(AnswerChecker.check(question, Answer(numericInput = "")).isCorrect)
        assertFalse(AnswerChecker.check(question, Answer(numericInput = "four")).isCorrect)
    }

    @Test
    fun `numerical accepts signed input`() {
        val question = question(type = QuestionType.NUMERICAL, correctValue = -3.0)

        assertTrue(AnswerChecker.check(question, Answer(numericInput = "-3")).isCorrect)
    }
}
