package com.example.mathongoassignment.data.mapper

import com.example.mathongoassignment.data.dto.ExamDto
import com.example.mathongoassignment.data.dto.OptionDto
import com.example.mathongoassignment.data.dto.QuestionDto
import com.example.mathongoassignment.domain.model.Option
import com.example.mathongoassignment.domain.model.Question
import com.example.mathongoassignment.domain.model.QuestionBank
import com.example.mathongoassignment.domain.model.QuestionType
import com.example.mathongoassignment.domain.model.RichContent
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull

object QuestionBankMapper {

    fun map(exam: ExamDto, includeTypes: Set<QuestionType>): QuestionBank {
        val examTitle = exam.title.orEmpty()
        val questions = buildList {
            exam.subjects.forEach { subject ->
                subject.chapters.forEach { chapter ->
                    chapter.questions.forEach { dto ->
                        if (dto.isRemoved) return@forEach
                        val type = dto.type.toQuestionType()
                        if (type !in includeTypes) return@forEach
                        add(
                            dto.toQuestion(
                                type = type,
                                examTitle = examTitle,
                                subjectTitle = subject.title.orEmpty(),
                                chapterTitle = chapter.title ?: chapter.chapterId.orEmpty(),
                            ),
                        )
                    }
                }
            }
        }
        return QuestionBank(examTitle = examTitle, questions = questions)
    }

    private fun QuestionDto.toQuestion(
        type: QuestionType,
        examTitle: String,
        subjectTitle: String,
        chapterTitle: String,
    ): Question {
        val correctValueLabel = correctValue?.contentOrNull?.takeIf { it.isNotBlank() }
        return Question(
            id = id?.oid ?: "$chapterTitle-${question?.text?.hashCode()}",
            type = type,
            content = contentWithImage(question?.text, question?.image),
            options = options.mapIndexedNotNull { index, option -> option.toOption(index) },
            correctValue = correctValue?.doubleOrNull ?: correctValueLabel?.trim()?.toDoubleOrNull(),
            correctValueLabel = correctValueLabel,
            numericalLowerLimit = numericalLowerLimit,
            numericalUpperLimit = numericalUpperLimit,
            previousYearPapers = previousYearPapers.filter { it.isNotBlank() },
            hasVideoSolution = isVideoSolutionAvailable,
            examTitle = examTitle,
            subjectTitle = subjectTitle,
            chapterTitle = chapterTitle,
        )
    }

    private fun OptionDto.toOption(index: Int): Option? {
        val content = contentWithImage(text, image)
        if (content.isEmpty) return null
        return Option(
            id = id?.takeIf { it.isNotBlank() } ?: "option-$index",
            content = content,
            isCorrect = isCorrect,
        )
    }

    private fun contentWithImage(text: String?, imageUrl: String?): RichContent {
        val content = ContentPreparer.prepare(text)
        val url = imageUrl?.takeIf { it.isNotBlank() } ?: return content
        return content.copy(html = content.html + "<img src=\"${url.escapeHtmlAttribute()}\" alt=\"\">", plainText = null)
    }

    private fun String.escapeHtmlAttribute(): String =
        replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;")

    private fun String?.toQuestionType(): QuestionType = when (this) {
        "singleCorrect" -> QuestionType.SINGLE_CORRECT
        "multipleCorrect" -> QuestionType.MULTIPLE_CORRECT
        "numerical" -> QuestionType.NUMERICAL
        else -> QuestionType.UNKNOWN
    }
}
