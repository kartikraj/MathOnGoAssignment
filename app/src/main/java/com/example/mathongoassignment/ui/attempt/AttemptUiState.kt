package com.example.mathongoassignment.ui.attempt

import androidx.annotation.StringRes
import com.example.mathongoassignment.domain.answer.Answer
import com.example.mathongoassignment.domain.answer.AnswerChecker
import com.example.mathongoassignment.domain.answer.AnswerResult
import com.example.mathongoassignment.domain.model.Question
import com.example.mathongoassignment.domain.model.QuestionBank

sealed interface AttemptUiState {

    data object Loading : AttemptUiState

    data class Error(@param:StringRes val messageRes: Int) : AttemptUiState

    data class Ready(
        val bank: QuestionBank,
        val currentIndex: Int,
        val answers: Map<String, Answer>,
    ) : AttemptUiState {
        val question: Question get() = bank.questions[currentIndex]
        val examTitle: String get() = bank.examTitle
        val questionNumber: Int get() = currentIndex + 1
        val totalQuestions: Int get() = bank.questions.size

        val answer: Answer get() = answers[question.id] ?: Answer()
        val selectedOptionIds: Set<String> get() = answer.selectedOptionIds
        val numericInput: String get() = answer.numericInput
        val isChecked: Boolean get() = answer.isChecked
        val result: AnswerResult? get() = if (isChecked) AnswerChecker.check(question, answer) else null

        val canGoPrevious: Boolean get() = currentIndex > 0
        val canGoNext: Boolean get() = currentIndex < bank.questions.lastIndex

        /** Check Answer stays disabled until something is answered, and after it has been checked. */
        val isCheckEnabled: Boolean get() = !answer.isEmpty(question.type) && !isChecked
    }
}
