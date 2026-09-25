package com.example.mathongoassignment.domain.model

enum class QuestionType {
    SINGLE_CORRECT,
    MULTIPLE_CORRECT,
    NUMERICAL,
    UNKNOWN,
}

data class RichContent(
    val html: String,
    val plainText: String?,
) {
    val isEmpty: Boolean get() = html.isBlank()
    val isPlainText: Boolean get() = plainText != null
}

data class Option(
    val id: String,
    val content: RichContent,
    val isCorrect: Boolean,
)

data class Question(
    val id: String,
    val type: QuestionType,
    val content: RichContent,
    val options: List<Option>,
    val correctValue: Double?,
    val correctValueLabel: String?,
    val numericalLowerLimit: Double?,
    val numericalUpperLimit: Double?,
    val previousYearPapers: List<String>,
    val hasVideoSolution: Boolean,
    val examTitle: String,
    val subjectTitle: String,
    val chapterTitle: String,
) {
    val correctOptionIds: Set<String>
        get() = options.filter { it.isCorrect }.map { it.id }.toSet()

    /** Source line shown under the question meta row, e.g. "JEE Advanced 2016 (Paper 1)". */
    val sourceLabel: String?
        get() = previousYearPapers.takeIf { it.isNotEmpty() }?.joinToString(" • ")
}

data class QuestionBank(
    val examTitle: String,
    val questions: List<Question>,
)
