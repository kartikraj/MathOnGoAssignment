package com.example.mathongoassignment.ui.attempt

import com.example.mathongoassignment.domain.answer.Answer
import com.example.mathongoassignment.domain.model.Question
import com.example.mathongoassignment.domain.model.QuestionType

internal object AttemptReducer {

    fun reduce(state: AttemptUiState, action: AttemptAction): AttemptUiState {
        if (state !is AttemptUiState.Ready) return state
        return when (action) {
            is AttemptAction.OptionClicked -> updateAnswer(state) { answer, question ->
                answer.copy(selectedOptionIds = toggle(answer.selectedOptionIds, action.optionId, question))
            }
            is AttemptAction.NumericInputChanged -> updateAnswer(state) { answer, _ ->
                answer.copy(numericInput = action.input)
            }
            AttemptAction.CheckAnswer -> updateAnswer(state) { answer, question ->
                if (answer.isEmpty(question.type)) answer else answer.copy(isChecked = true)
            }
            AttemptAction.Previous -> moveTo(state, state.currentIndex - 1)
            AttemptAction.Next -> moveTo(state, state.currentIndex + 1)
            AttemptAction.Retry -> state
        }
    }

    private fun toggle(selected: Set<String>, optionId: String, question: Question): Set<String> =
        when (question.type) {
            QuestionType.MULTIPLE_CORRECT -> if (optionId in selected) selected - optionId else selected + optionId
            // Single correct allows exactly one selection at a time.
            else -> setOf(optionId)
        }

    private fun updateAnswer(
        state: AttemptUiState.Ready,
        transform: (Answer, Question) -> Answer,
    ): AttemptUiState.Ready {
        val answer = state.answer
        if (answer.isChecked) return state // The answer is locked once it has been checked.
        val updated = transform(answer, state.question)
        if (updated == answer) return state
        return state.copy(answers = state.answers + (state.question.id to updated))
    }

    private fun moveTo(state: AttemptUiState.Ready, index: Int): AttemptUiState.Ready =
        if (index in state.bank.questions.indices) state.copy(currentIndex = index) else state
}
