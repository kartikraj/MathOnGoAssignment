package com.example.mathongoassignment.ui

import androidx.lifecycle.SavedStateHandle
import com.example.mathongoassignment.FakeQuestionBankRepository
import com.example.mathongoassignment.R
import com.example.mathongoassignment.domain.model.QuestionBank
import com.example.mathongoassignment.domain.repository.QuestionBankException
import com.example.mathongoassignment.option
import com.example.mathongoassignment.question
import com.example.mathongoassignment.ui.attempt.AttemptAction
import com.example.mathongoassignment.ui.attempt.AttemptUiState
import com.example.mathongoassignment.ui.attempt.AttemptViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AttemptViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(savedState: SavedStateHandle = SavedStateHandle()) = AttemptViewModel(
        repository = FakeQuestionBankRepository(Result.success(BANK)),
        savedStateHandle = savedState,
    )

    private fun AttemptViewModel.ready(): AttemptUiState.Ready = uiState.value as AttemptUiState.Ready

    @Test
    fun `starts on the first question with check disabled`() = runTest {
        val state = viewModel().ready()

        assertEquals(1, state.questionNumber)
        assertEquals(2, state.totalQuestions)
        assertFalse(state.isCheckEnabled)
        assertFalse(state.canGoPrevious)
        assertTrue(state.canGoNext)
    }

    @Test
    fun `selecting an option enables check and does not reveal the answer`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(AttemptAction.OptionClicked("a"))

        val state = viewModel.ready()
        assertEquals(setOf("a"), state.selectedOptionIds)
        assertTrue(state.isCheckEnabled)
        assertFalse(state.isChecked)
        assertEquals(null, state.result)
    }

    @Test
    fun `single correct keeps only one selection`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(AttemptAction.OptionClicked("a"))
        viewModel.onAction(AttemptAction.OptionClicked("b"))

        assertEquals(setOf("b"), viewModel.ready().selectedOptionIds)
    }

    @Test
    fun `checking reveals the result and locks the answer`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(AttemptAction.OptionClicked("a"))
        viewModel.onAction(AttemptAction.CheckAnswer)

        val checked = viewModel.ready()
        assertTrue(checked.isChecked)
        assertFalse(checked.result!!.isCorrect)
        assertEquals(setOf("b"), checked.result!!.correctOptionIds)
        assertFalse(checked.isCheckEnabled)

        viewModel.onAction(AttemptAction.OptionClicked("b"))
        assertEquals(setOf("a"), viewModel.ready().selectedOptionIds)
    }

    @Test
    fun `answer state is restored when coming back to a question`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(AttemptAction.OptionClicked("a"))
        viewModel.onAction(AttemptAction.CheckAnswer)
        viewModel.onAction(AttemptAction.Next)

        val second = viewModel.ready()
        assertEquals(2, second.questionNumber)
        assertTrue(second.selectedOptionIds.isEmpty())
        assertFalse(second.isChecked)
        assertFalse(second.canGoNext)

        viewModel.onAction(AttemptAction.Previous)

        val first = viewModel.ready()
        assertEquals(setOf("a"), first.selectedOptionIds)
        assertTrue(first.isChecked)
    }

    @Test
    fun `answers survive a screen recreation through saved state`() = runTest {
        val savedState = SavedStateHandle()
        val viewModel = viewModel(savedState)
        viewModel.onAction(AttemptAction.OptionClicked("b"))
        viewModel.onAction(AttemptAction.CheckAnswer)
        viewModel.onAction(AttemptAction.Next)

        // A new ViewModel built from the same saved state stands in for the recreated screen.
        val recreated = viewModel(savedState)

        val state = recreated.ready()
        assertEquals(2, state.questionNumber)
        recreated.onAction(AttemptAction.Previous)
        assertEquals(setOf("b"), recreated.ready().selectedOptionIds)
        assertTrue(recreated.ready().isChecked)
    }

    @Test
    fun `a restored index past the end is clamped to the last question`() = runTest {
        val state = viewModel(SavedStateHandle(mapOf("current_question_index" to 7))).ready()

        assertEquals(2, state.questionNumber)
    }

    private fun failingViewModel(error: Throwable) = AttemptViewModel(
        repository = FakeQuestionBankRepository(Result.failure(error)),
        savedStateHandle = SavedStateHandle(),
    )

    @Test
    fun `a load failure shows the screen's own message, not the exception's`() = runTest {
        val state = failingViewModel(QuestionBankException("Could not read the question data."))
            .uiState.value

        assertEquals(AttemptUiState.Error(R.string.error_data_unreadable), state)
    }

    @Test
    fun `an unexpected failure shows a generic message`() = runTest {
        val state = failingViewModel(IllegalStateException("boom")).uiState.value

        assertEquals(AttemptUiState.Error(R.string.error_unexpected), state)
    }

    private companion object {
        val BANK = QuestionBank(
            examTitle = "JEE Advanced",
            questions = listOf(
                question(id = "q1", options = listOf(option("a", false), option("b", true))),
                question(id = "q2", options = listOf(option("c", true), option("d", false))),
            ),
        )
    }
}
