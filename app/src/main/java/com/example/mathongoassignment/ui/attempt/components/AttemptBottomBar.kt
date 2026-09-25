package com.example.mathongoassignment.ui.attempt.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mathongoassignment.R
import com.example.mathongoassignment.ui.theme.appColors

@Composable
fun AttemptBottomBar(
    isCheckEnabled: Boolean,
    isPreviousEnabled: Boolean,
    isNextEnabled: Boolean,
    onPrevious: () -> Unit,
    onCheckAnswer: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = appColors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bottomBar)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedPillButton(
            text = stringResource(R.string.action_previous),
            enabled = isPreviousEnabled,
            onClick = onPrevious,
            modifier = Modifier.weight(1f),
        )
        FilledPillButton(
            text = stringResource(R.string.action_check_answer),
            enabled = isCheckEnabled,
            onClick = onCheckAnswer,
            modifier = Modifier.weight(1.5f),
        )
        OutlinedPillButton(
            text = stringResource(R.string.action_next),
            enabled = isNextEnabled,
            onClick = onNext,
            modifier = Modifier.weight(1f),
        )
    }
}

private val PillShape = RoundedCornerShape(24.dp)
private val PillHeight = 46.dp

@Composable
private fun OutlinedPillButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = appColors
    PillContainer(
        modifier = modifier,
        background = colors.card,
        border = colors.cardBorder,
        enabled = enabled,
        onClick = onClick,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) colors.textPrimary else colors.textSecondary.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun FilledPillButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = appColors
    PillContainer(
        modifier = modifier,
        background = if (enabled) colors.accent else colors.accentDisabled,
        border = Color.Transparent,
        enabled = enabled,
        onClick = onClick,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            // On the dark palette a full-strength label would read as an enabled button.
            color = if (enabled || !colors.isDark) colors.onAccent else colors.textSecondary,
        )
    }
}

@Composable
private fun PillContainer(
    modifier: Modifier,
    background: Color,
    border: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .height(PillHeight)
            .clip(PillShape)
            .background(background)
            .border(1.dp, border, PillShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}
