package com.example.mathongoassignment.domain.repository

import com.example.mathongoassignment.domain.model.QuestionBank

interface QuestionBankRepository {

    suspend fun loadQuestionBank(): Result<QuestionBank>
}

class QuestionBankException(message: String, cause: Throwable? = null) : Exception(message, cause)
