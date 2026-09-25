package com.example.mathongoassignment.data.mapper

import com.example.mathongoassignment.domain.model.RichContent

object ContentPreparer {

    private val HTML_TAG = Regex("<\\s*/?\\s*[a-zA-Z][a-zA-Z0-9]*[^>]*>")
    private val LATEX_DELIMITER = Regex("\\$|\\\\\\(|\\\\\\[|\\\\begin\\{")
    private val NUMERIC_ENTITY = Regex("&#(x?)([0-9a-fA-F]+);")
    private val ANY_ENTITY = Regex("&#?[a-zA-Z0-9]+;")
    private val WHITESPACE = Regex("\\s+")
    private val BLANK_LINES = Regex("\\n\\s*\\n")

    private val NAMED_ENTITIES = mapOf(
        "nbsp" to " ", "amp" to "&", "lt" to "<", "gt" to ">", "quot" to "\"",
        "apos" to "'", "times" to "×", "divide" to "÷", "deg" to "°",
        "plusmn" to "±", "hellip" to "…", "rarr" to "→", "larr" to "←",
        "alpha" to "α", "beta" to "β", "gamma" to "γ", "delta" to "δ",
        "theta" to "θ", "lambda" to "λ", "mu" to "μ", "pi" to "π",
        "rho" to "ρ", "sigma" to "σ", "omega" to "ω", "infin" to "∞",
    )

    fun prepare(raw: String?): RichContent {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty()) return RichContent(html = "", plainText = "")

        val html = normalizeHtml(text)
        if (needsRichRendering(text)) return RichContent(html = html, plainText = null)

        val plainText = toPlainText(text)
        return if (ANY_ENTITY.containsMatchIn(plainText)) {
            RichContent(html = html, plainText = null)
        } else {
            RichContent(html = html, plainText = plainText)
        }
    }

    private fun needsRichRendering(text: String): Boolean =
        HTML_TAG.containsMatchIn(text) || LATEX_DELIMITER.containsMatchIn(text)

    private fun normalizeHtml(text: String): String {
        if (HTML_TAG.containsMatchIn(text)) return text
        return BLANK_LINES.replace(text, "<br><br>").replace("\n", " ")
    }

    private fun toPlainText(text: String): String =
        WHITESPACE.replace(decodeEntities(text), " ").trim()

    fun decodeEntities(text: String): String {
        if (!text.contains('&')) return text
        var result = NUMERIC_ENTITY.replace(text) { match ->
            val radix = if (match.groupValues[1].isEmpty()) 10 else 16
            val code = match.groupValues[2].toIntOrNull(radix)
            if (code != null && code in 1..0x10FFFF) String(Character.toChars(code)) else match.value
        }
        NAMED_ENTITIES.forEach { (name, value) -> result = result.replace("&$name;", value) }
        return result
    }
}
