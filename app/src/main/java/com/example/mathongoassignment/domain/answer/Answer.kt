package com.example.mathongoassignment.domain.answer

import com.example.mathongoassignment.domain.model.QuestionType
import kotlinx.serialization.Serializable

@Serializable
data class Answer(
    val selectedOptionIds: Set<String> = emptySet(),
    val numericInput: String = "",
    val isChecked: Boolean = false,
) {
    fun isEmpty(type: QuestionType): Boolean =
        if (type == QuestionType.NUMERICAL) numericInput.trim().toDoubleOrNull() == null
        else selectedOptionIds.isEmpty()
}

data class AnswerResult(
    val isCorrect: Boolean,
    val correctOptionIds: Set<String>,
    val correctAnswerLabel: String?,
)
