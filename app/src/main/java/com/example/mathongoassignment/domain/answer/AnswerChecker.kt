package com.example.mathongoassignment.domain.answer

import com.example.mathongoassignment.domain.model.Question
import com.example.mathongoassignment.domain.model.QuestionType
import kotlin.math.abs

object AnswerChecker {

    /** Tolerance used when a numerical question gives an exact value instead of a range. */
    private const val NUMERIC_TOLERANCE = 1e-6

    fun check(question: Question, answer: Answer): AnswerResult = when (question.type) {
        QuestionType.SINGLE_CORRECT -> checkSingleCorrect(question, answer)
        QuestionType.MULTIPLE_CORRECT -> checkMultipleCorrect(question, answer)
        QuestionType.NUMERICAL -> checkNumerical(question, answer)
        QuestionType.UNKNOWN -> AnswerResult(
            isCorrect = false,
            correctOptionIds = question.correctOptionIds,
            correctAnswerLabel = null,
        )
    }

    /** Exactly one option may be selected and it has to be the one flagged correct in the data. */
    private fun checkSingleCorrect(question: Question, answer: Answer): AnswerResult {
        val selected = answer.selectedOptionIds
        val correct = question.correctOptionIds
        val isCorrect = selected.size == 1 && correct.size == 1 && selected == correct
        return AnswerResult(isCorrect, correct, correctAnswerLabel = null)
    }

    /** Correct only when the selected set matches the complete set of correct options. */
    private fun checkMultipleCorrect(question: Question, answer: Answer): AnswerResult {
        val selected = answer.selectedOptionIds
        val correct = question.correctOptionIds
        val isCorrect = correct.isNotEmpty() && selected == correct
        return AnswerResult(isCorrect, correct, correctAnswerLabel = null)
    }

    /**
     * Uses the inclusive [Question.numericalLowerLimit]/[Question.numericalUpperLimit] range when
     * the data provides one, and falls back to comparing against `correctValue`.
     */
    private fun checkNumerical(question: Question, answer: Answer): AnswerResult {
        val entered = answer.numericInput.trim().toDoubleOrNull()
        val lower = question.numericalLowerLimit
        val upper = question.numericalUpperLimit
        val expected = question.correctValue

        val isCorrect = when {
            entered == null -> false
            lower != null && upper != null -> entered >= minOf(lower, upper) && entered <= maxOf(lower, upper)
            lower != null -> entered >= lower
            upper != null -> entered <= upper
            expected != null -> abs(entered - expected) <= NUMERIC_TOLERANCE
            else -> false
        }
        return AnswerResult(
            isCorrect = isCorrect,
            correctOptionIds = emptySet(),
            correctAnswerLabel = numericalAnswerLabel(question),
        )
    }

    /** Human readable answer for the numerical feedback row. */
    fun numericalAnswerLabel(question: Question): String? {
        val lower = question.numericalLowerLimit
        val upper = question.numericalUpperLimit
        return when {
            question.correctValueLabel != null -> question.correctValueLabel
            lower != null && upper != null && lower != upper -> "${format(lower)} to ${format(upper)}"
            lower != null -> format(lower)
            upper != null -> format(upper)
            else -> null
        }
    }

    private fun format(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
}
