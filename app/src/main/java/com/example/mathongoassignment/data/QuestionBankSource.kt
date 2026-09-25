package com.example.mathongoassignment.data

import android.content.Context
import java.io.InputStream

fun interface QuestionBankSource {
    fun open(): InputStream
}

class AssetQuestionBankSource(
    private val context: Context,
    private val fileName: String = "data.json",
) : QuestionBankSource {
    override fun open(): InputStream = context.assets.open(fileName)
}
