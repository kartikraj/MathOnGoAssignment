package com.example.mathongoassignment.ui.richtext

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mathongoassignment.domain.model.RichContent
import com.example.mathongoassignment.ui.theme.appColors

@Composable
fun RichContentView(
    content: RichContent,
    slotKey: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = appColors.textPrimary,
    placeholderHeight: Dp = 28.dp,
) {
    if (content.isEmpty) return

    if (!content.needsWebView) {
        Text(text = content.plainText.orEmpty(), style = textStyle, color = color, modifier = modifier)
        return
    }

    val colors = appColors
    val pool = LocalContentWebViewPool.current
    val holder = remember(slotKey) { pool.acquire(slotKey) }
    val contentTheme = remember(color, colors, textStyle) {
        ContentTheme(
            textColor = color.toCssHex(),
            mutedColor = colors.textSecondary.toCssHex(),
            borderColor = colors.cardBorder.toCssHex(),
            fontSizePx = textStyle.fontSize.value.toInt(),
            lineHeight = textStyle.lineHeight.value / textStyle.fontSize.value,
        )
    }

    LaunchedEffect(holder, content.html, contentTheme) {
        holder.setContent(content.html, contentTheme)
    }

    // The page measures itself and reports back; until then a placeholder holds the space.
    val measuredHeight by holder.contentHeightCssPx.collectAsState()
    val height = if (measuredHeight > 0) measuredHeight.dp else placeholderHeight

    Box(modifier.fillMaxWidth().height(height)) {
        val webView by holder.webView.collectAsState()
        key(webView) {
            AndroidView(
                factory = { webView },
                modifier = Modifier.fillMaxWidth().height(height),
            )
        }
        if (measuredHeight == 0) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(placeholderHeight)
                    .background(colors.badgeBackground, RoundedCornerShape(6.dp)),
            )
        }
    }
}

internal val RichContent.needsWebView: Boolean get() = !isPlainText

private fun Color.toCssHex(): String = String.format("#%06X", toArgb() and 0xFFFFFF)
