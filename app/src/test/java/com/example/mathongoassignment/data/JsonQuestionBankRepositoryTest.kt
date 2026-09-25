package com.example.mathongoassignment.data

import com.example.mathongoassignment.domain.model.QuestionType
import com.example.mathongoassignment.domain.repository.QuestionBankException
import java.io.FileNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonQuestionBankRepositoryTest {

    private fun repository(
        payload: String,
        types: Set<QuestionType> = JsonQuestionBankRepository.DEFAULT_TYPES,
    ) = JsonQuestionBankRepository(
        source = { payload.byteInputStream() },
        dispatcher = Dispatchers.Unconfined,
        includeTypes = types,
    )

    @Test
    fun `parses the exam tree into a flat ordered list`() = runTest {
        val bank = repository(SAMPLE).loadQuestionBank().getOrThrow()

        assertEquals("JEE Advanced", bank.examTitle)
        assertEquals(listOf("single-1", "numerical-1"), bank.questions.map { it.id })
        val first = bank.questions.first()
        assertEquals(QuestionType.SINGLE_CORRECT, first.type)
        assertEquals("Units and Dimensions", first.chapterTitle)
        assertEquals("Physics", first.subjectTitle)
        assertEquals(setOf("b"), first.correctOptionIds)
        assertEquals("JEE Advanced 2016 (Paper 1)", first.sourceLabel)
        assertTrue(first.hasVideoSolution)
    }

    @Test
    fun `keeps only the requested question types`() = runTest {
        val bank = repository(SAMPLE, setOf(QuestionType.SINGLE_CORRECT)).loadQuestionBank().getOrThrow()

        assertEquals(1, bank.questions.size)
    }

    @Test
    fun `reads a numerical correct value stored as a string`() = runTest {
        val bank = repository(SAMPLE).loadQuestionBank().getOrThrow()
        val numerical = bank.questions.last()

        assertEquals(QuestionType.NUMERICAL, numerical.type)
        assertEquals(4.5, numerical.correctValue!!, 1e-9)
        assertEquals(4.0, numerical.numericalLowerLimit!!, 1e-9)
    }

    @Test
    fun `unknown fields and missing optional fields do not break parsing`() = runTest {
        val bank = repository(MINIMAL).loadQuestionBank().getOrThrow()

        val question = bank.questions.single()
        assertEquals(null, question.sourceLabel)
        assertEquals(false, question.hasVideoSolution)
    }

    @Test
    fun `broken json produces a failure instead of a crash`() = runTest {
        val result = repository("{ not json at all").loadQuestionBank()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is QuestionBankException)
    }

    @Test
    fun `a file with no usable questions is a failure`() = runTest {
        val result = repository("""{"title":"JEE","subjects":[]}""").loadQuestionBank()

        assertTrue(result.isFailure)
    }

    @Test
    fun `a missing file is a failure`() = runTest {
        val result = JsonQuestionBankRepository(
            source = { throw FileNotFoundException("data.json") },
            dispatcher = Dispatchers.Unconfined,
        ).loadQuestionBank()

        assertTrue(result.exceptionOrNull() is QuestionBankException)
    }

    @Test
    fun `json of the wrong shape is a failure`() = runTest {
        val result = repository("""{"title": "JEE", "subjects": "not a list"}""").loadQuestionBank()

        assertTrue(result.exceptionOrNull() is QuestionBankException)
    }

    @Test(expected = IllegalStateException::class)
    fun `an unexpected error is not reported as bad data`() = runTest {
        JsonQuestionBankRepository(
            source = { error("bug") },
            dispatcher = Dispatchers.Unconfined,
        ).loadQuestionBank()
    }

    private companion object {
        val SAMPLE = """
        {
          "_id": {"${'$'}oid": "exam1"},
          "title": "JEE Advanced",
          "unexpectedField": 42,
          "subjects": [
            {
              "title": "Physics",
              "chapters": [
                {
                  "title": "Units and Dimensions",
                  "class": "Class 11",
                  "questions": [
                    {
                      "_id": {"${'$'}oid": "single-1"},
                      "type": "singleCorrect",
                      "question": {"text": "A question", "image": null},
                      "options": [
                        {"id": "a", "text": "Wrong", "image": null, "isCorrect": false},
                        {"id": "b", "text": "Right", "image": null, "isCorrect": true}
                      ],
                      "correctValue": null,
                      "previousYearPapers": ["JEE Advanced 2016 (Paper 1)"],
                      "isVideoSolutionAvailable": true
                    },
                    {
                      "_id": {"${'$'}oid": "removed-1"},
                      "type": "singleCorrect",
                      "question": {"text": "Removed"},
                      "options": [],
                      "isRemoved": true
                    },
                    {
                      "_id": {"${'$'}oid": "numerical-1"},
                      "type": "numerical",
                      "question": {"text": "A numerical question"},
                      "options": [],
                      "correctValue": "4.5",
                      "numericalLowerLimit": 4.0,
                      "numericalUpperLimit": 5.0,
                      "previousYearPapers": [],
                      "isVideoSolutionAvailable": false
                    }
                  ]
                }
              ]
            }
          ]
        }
        """.trimIndent()

        val MINIMAL = """
        {
          "title": "JEE Advanced",
          "subjects": [
            {"title": "Physics", "chapters": [
              {"title": "Chapter", "questions": [
                {"_id": {"${'$'}oid": "q"}, "type": "singleCorrect", "question": {"text": "Text"},
                 "options": [{"id": "a", "text": "A", "isCorrect": true}]}
              ]}
            ]}
          ]
        }
        """.trimIndent()
    }
}
