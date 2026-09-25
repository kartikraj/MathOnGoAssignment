package com.example.mathongoassignment.data

import com.example.mathongoassignment.data.mapper.ContentPreparer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentPreparerTest {

    @Test
    fun `plain prose is classified as plain text`() {
        val content = ContentPreparer.prepare("Which of the following is correct?")

        assertNotNull(content.plainText)
        assertTrue(content.isPlainText)
    }

    @Test
    fun `entities are decoded in the plain text`() {
        val content = ContentPreparer.prepare("Pressure &#8594; volume&nbsp;relation &amp; more")

        assertEquals("Pressure → volume relation & more", content.plainText)
    }

    @Test
    fun `an entity it cannot decode is not plain text`() {
        assertFalse(ContentPreparer.prepare("x &le; y").isPlainText)
    }

    @Test
    fun `latex is not plain text`() {
        val content = ContentPreparer.prepare("Find $\\frac{a}{b}$ for the given data")

        assertNull(content.plainText)
        assertFalse(content.isPlainText)
    }

    @Test
    fun `mathml is not plain text`() {
        val content = ContentPreparer.prepare(
            "Using <math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>d</mi></math> here",
        )

        assertFalse(content.isPlainText)
    }

    @Test
    fun `html markup is not plain text`() {
        assertFalse(ContentPreparer.prepare("<p>Two <strong>statements</strong></p>").isPlainText)
        assertFalse(ContentPreparer.prepare("A table <table><tr><td>1</td></tr></table>").isPlainText)
    }

    @Test
    fun `blank lines become breaks when there is no markup`() {
        val content = ContentPreparer.prepare("First line\n\nSecond line")

        assertEquals("First line<br><br>Second line", content.html)
    }

    @Test
    fun `null and blank content is empty`() {
        assertTrue(ContentPreparer.prepare(null).isEmpty)
        assertTrue(ContentPreparer.prepare("   ").isEmpty)
    }
}
