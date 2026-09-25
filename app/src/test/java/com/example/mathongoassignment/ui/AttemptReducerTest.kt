package com.example.mathongoassignment.ui

import com.example.mathongoassignment.R
import com.example.mathongoassignment.domain.model.QuestionBank
import com.example.mathongoassignment.domain.model.QuestionType
import com.example.mathongoassignment.option
import com.example.mathongoassignment.question
import com.example.mathongoassignment.ui.attempt.AttemptAction
import com.example.mathongoassignment.ui.attempt.AttemptReducer
import com.example.mathongoassignment.ui.attempt.AttemptUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AttemptReducerTest {

    private fun ready(index: Int = 0) = AttemptUiState.Ready(BANK, currentIndex = index, answers = emptyMap())

    private fun AttemptUiState.after(vararg actions: AttemptAction): AttemptUiState =
        actions.fold(this) { state, action -> AttemptReducer.reduce(state, action) }

    private fun AttemptUiState.asReady() = this as AttemptUiState.Ready

    @Test
    fun `multiple correct toggles options on and off`() {
        val state = ready(index = 1).after(
            AttemptAction.OptionClicked("x"),
            AttemptAction.OptionClicked("y"),
            AttemptAction.OptionClicked("x"),
        )

        assertEquals(setOf("y"), state.asReady().selectedOptionIds)
    }

    @Test
    fun `check without an answer changes nothing`() {
        val state = ready()

        assertSame(state, state.after(AttemptAction.CheckAnswer))
    }

    @Test
    fun `a partial numeric entry cannot be checked`() {
        val numerical = AttemptUiState.Ready(NUMERICAL_BANK, currentIndex = 0, answers = emptyMap())

        for (partial in listOf("-", "+", ".", "-.")) {
            val typed = numerical.after(AttemptAction.NumericInputChanged(partial))
            assertEquals(false, typed.asReady().isCheckEnabled)
            assertSame(typed, typed.after(AttemptAction.CheckAnswer))
        }
        val complete = numerical.after(AttemptAction.NumericInputChanged("-557"), AttemptAction.CheckAnswer)
        assertEquals(true, complete.asReady().isChecked)
    }

    @Test
    fun `a checked answer ignores further input`() {
        val checked = ready().after(AttemptAction.OptionClicked("a"), AttemptAction.CheckAnswer)

        assertSame(checked, checked.after(AttemptAction.OptionClicked("b")))
    }

    @Test
    fun `navigation stops at both ends`() {
        val first = ready()
        val last = first.after(AttemptAction.Next)

        assertSame(first, first.after(AttemptAction.Previous))
        assertSame(last, last.after(AttemptAction.Next))
        assertEquals(1, last.asReady().currentIndex)
    }

    @Test
    fun `answers are kept per question while navigating`() {
        val state = ready().after(
            AttemptAction.OptionClicked("a"),
            AttemptAction.Next,
            AttemptAction.OptionClicked("y"),
            AttemptAction.Previous,
        )

        assertEquals(setOf("a"), state.asReady().selectedOptionIds)
        assertEquals(setOf("y"), state.asReady().answers.getValue("q2").selectedOptionIds)
    }

    @Test
    fun `actions while loading or failed change nothing`() {
        val loading = AttemptUiState.Loading
        val failed = AttemptUiState.Error(R.string.error_data_unreadable)

        assertSame(loading, loading.after(AttemptAction.OptionClicked("a"), AttemptAction.Next))
        assertSame(failed, failed.after(AttemptAction.CheckAnswer))
    }

    private companion object {
        val NUMERICAL_BANK = QuestionBank(
            examTitle = "JEE Advanced",
            questions = listOf(question(id = "n1", type = QuestionType.NUMERICAL, correctValue = -557.0)),
        )

        val BANK = QuestionBank(
            examTitle = "JEE Advanced",
            questions = listOf(
                question(id = "q1", options = listOf(option("a", false), option("b", true))),
                question(
                    id = "q2",
                    type = QuestionType.MULTIPLE_CORRECT,
                    options = listOf(option("x", true), option("y", true)),
                ),
            ),
        )
    }
}
