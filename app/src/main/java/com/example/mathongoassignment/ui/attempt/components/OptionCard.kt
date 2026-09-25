package com.example.mathongoassignment.ui.attempt.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mathongoassignment.R
import com.example.mathongoassignment.domain.model.RichContent
import com.example.mathongoassignment.ui.richtext.RichContentView
import com.example.mathongoassignment.ui.theme.appColors

enum class OptionState { Default, Selected, Correct, Incorrect }

private val CardShape = RoundedCornerShape(10.dp)

@Composable
fun OptionCard(
    label: String,
    content: RichContent,
    slotKey: String,
    state: OptionState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = appColors
    val (borderColor, backgroundColor, badgeColor) = when (state) {
        OptionState.Default -> Triple(colors.cardBorder, colors.card, colors.badgeBackground)
        OptionState.Selected -> Triple(colors.accent, colors.accentSurface, colors.accent)
        OptionState.Correct -> Triple(colors.correct, colors.correctSurface, colors.correct)
        OptionState.Incorrect -> Triple(colors.incorrect, colors.incorrectSurface, colors.incorrect)
    }
    val badgeTextColor = if (state == OptionState.Default) colors.badgeText else colors.onAccent
    val banner = when (state) {
        OptionState.Correct -> BannerStyle(stringResource(R.string.option_correct_answer), R.drawable.ic_check, colors.correct)
        OptionState.Incorrect -> BannerStyle(stringResource(R.string.option_your_answer), R.drawable.ic_close, colors.incorrect)
        else -> null
    }

    Box(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (banner != null) BannerOverlap else 0.dp)
                .clip(CardShape)
                .background(backgroundColor)
                .border(if (state == OptionState.Default) 1.dp else 1.5.dp, borderColor, CardShape)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .defaultMinSize(minHeight = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OptionBadge(label = label, background = badgeColor, textColor = badgeTextColor)
            Spacer(Modifier.width(12.dp))
            RichContentView(
                content = content,
                slotKey = slotKey,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
                placeholderHeight = 20.dp,
            )
        }

        // The banner sits on the card's top border, masking it the way the design shows.
        banner?.let {
            AnswerBanner(
                style = it,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 14.dp),
            )
        }
    }
}

private val BannerOverlap = 10.dp

private data class BannerStyle(val text: String, @param:DrawableRes val icon: Int, val color: Color)

@Composable
private fun AnswerBanner(style: BannerStyle, modifier: Modifier = Modifier) {
    val colors = appColors
    Row(
        modifier = modifier
            .background(colors.background)
            .padding(horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            text = style.text,
            style = MaterialTheme.typography.labelSmall,
            color = style.color,
        )
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(style.color, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(style.icon),
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier.size(13.dp),
            )
        }
    }
}

@Composable
private fun OptionBadge(label: String, background: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
        )
    }
}
