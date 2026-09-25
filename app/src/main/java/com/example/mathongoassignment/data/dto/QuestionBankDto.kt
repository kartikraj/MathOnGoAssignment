package com.example.mathongoassignment.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class ExamDto(
    @SerialName("_id") val id: ObjectIdDto? = null,
    val title: String? = null,
    val icon: String? = null,
    val subjects: List<SubjectDto> = emptyList(),
)

@Serializable
data class SubjectDto(
    @SerialName("_id") val id: ObjectIdDto? = null,
    val title: String? = null,
    val chapters: List<ChapterDto> = emptyList(),
)

@Serializable
data class ChapterDto(
    @SerialName("_id") val id: ObjectIdDto? = null,
    val chapterId: String? = null,
    val title: String? = null,
    @SerialName("class") val classLevel: String? = null,
    val questions: List<QuestionDto> = emptyList(),
)

@Serializable
data class QuestionDto(
    @SerialName("_id") val id: ObjectIdDto? = null,
    val type: String? = null,
    val question: QuestionContentDto? = null,
    val options: List<OptionDto> = emptyList(),
    /** Numerical answers appear either as a JSON string or as a JSON number. */
    val correctValue: JsonPrimitive? = null,
    val numericalLowerLimit: Double? = null,
    val numericalUpperLimit: Double? = null,
    val previousYearPapers: List<String> = emptyList(),
    val isVideoSolutionAvailable: Boolean = false,
    val videoSolution: VideoSolutionDto? = null,
    val isRemoved: Boolean = false,
)

@Serializable
data class QuestionContentDto(
    val text: String? = null,
    val image: String? = null,
)

@Serializable
data class OptionDto(
    val id: String? = null,
    val text: String? = null,
    val image: String? = null,
    val isCorrect: Boolean = false,
)

@Serializable
data class VideoSolutionDto(
    val videoId: String? = null,
    val provider: String? = null,
    val description: String? = null,
)

@Serializable
data class ObjectIdDto(
    @SerialName("\$oid") val oid: String? = null,
)
