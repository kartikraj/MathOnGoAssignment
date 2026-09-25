package com.example.mathongoassignment

import com.example.mathongoassignment.domain.model.QuestionBank
import com.example.mathongoassignment.domain.repository.QuestionBankRepository

class FakeQuestionBankRepository(private val result: Result<QuestionBank>) : QuestionBankRepository {
    override suspend fun loadQuestionBank(): Result<QuestionBank> = result
}
