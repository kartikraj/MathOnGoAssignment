package com.example.mathongoassignment.data

import com.example.mathongoassignment.data.dto.ExamDto
import com.example.mathongoassignment.data.mapper.QuestionBankMapper
import com.example.mathongoassignment.domain.model.QuestionBank
import com.example.mathongoassignment.domain.model.QuestionType
import com.example.mathongoassignment.domain.repository.QuestionBankException
import com.example.mathongoassignment.domain.repository.QuestionBankRepository
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

class JsonQuestionBankRepository(
    private val source: QuestionBankSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val includeTypes: Set<QuestionType> = DEFAULT_TYPES,
) : QuestionBankRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Volatile
    private var cached: QuestionBank? = null

    override suspend fun loadQuestionBank(): Result<QuestionBank> {
        cached?.let { return Result.success(it) }
        return withContext(dispatcher) {
            try {
                @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
                val exam = source.open().use { stream -> json.decodeFromStream<ExamDto>(stream) }
                val bank = QuestionBankMapper.map(exam, includeTypes)
                if (bank.questions.isEmpty()) {
                    Result.failure(QuestionBankException("The question file contains no usable questions."))
                } else {
                    cached = bank
                    Result.success(bank)
                }
            } catch (e: IOException) {
                Result.failure(QuestionBankException("Could not read the question data.", e))
            } catch (e: SerializationException) {
                Result.failure(QuestionBankException("Could not read the question data.", e))
            }
        }
    }

    companion object {
        val DEFAULT_TYPES = setOf(
            QuestionType.SINGLE_CORRECT,
            QuestionType.MULTIPLE_CORRECT,
            QuestionType.NUMERICAL,
        )
    }
}
