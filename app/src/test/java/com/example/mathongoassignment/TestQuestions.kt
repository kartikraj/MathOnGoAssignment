package com.example.mathongoassignment

import com.example.mathongoassignment.domain.model.Option
import com.example.mathongoassignment.domain.model.Question
import com.example.mathongoassignment.domain.model.QuestionType
import com.example.mathongoassignment.domain.model.RichContent

fun question(
    id: String = "q1",
    type: QuestionType = QuestionType.SINGLE_CORRECT,
    options: List<Option> = emptyList(),
    correctValue: Double? = null,
    correctValueLabel: String? = null,
    lowerLimit: Double? = null,
    upperLimit: Double? = null,
): Question = Question(
    id = id,
    type = type,
    content = RichContent(html = "Question", plainText = "Question"),
    options = options,
    correctValue = correctValue,
    correctValueLabel = correctValueLabel,
    numericalLowerLimit = lowerLimit,
    numericalUpperLimit = upperLimit,
    previousYearPapers = emptyList(),
    hasVideoSolution = false,
    examTitle = "JEE Advanced",
    subjectTitle = "Physics",
    chapterTitle = "Units and Dimensions",
)

fun option(id: String, isCorrect: Boolean): Option = Option(
    id = id,
    content = RichContent(html = "Option $id", plainText = "Option $id"),
    isCorrect = isCorrect,
)

fun options(vararg correctFlags: Boolean): List<Option> =
    correctFlags.mapIndexed { index, isCorrect ->
        Option(
            id = "opt$index",
            content = RichContent(html = "Option $index", plainText = "Option $index"),
            isCorrect = isCorrect,
        )
    }
