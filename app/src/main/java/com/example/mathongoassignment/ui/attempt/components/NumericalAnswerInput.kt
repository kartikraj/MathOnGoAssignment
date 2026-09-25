package com.example.mathongoassignment.ui.attempt.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mathongoassignment.R
import com.example.mathongoassignment.ui.theme.appColors

@Composable
fun NumericalAnswerInput(
    value: String,
    isChecked: Boolean,
    isCorrect: Boolean,
    correctAnswerLabel: String?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = appColors
    val borderColor = when {
        !isChecked && value.isNotBlank() -> colors.accent
        !isChecked -> colors.cardBorder
        isCorrect -> colors.correct
        else -> colors.incorrect
    }
    val backgroundColor = when {
        !isChecked -> colors.card
        isCorrect -> colors.correctSurface
        else -> colors.incorrectSurface
    }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.numeric_label),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
        )
        BasicTextField(
            value = value,
            onValueChange = { input -> onValueChange(input.filterNumeric()) },
            enabled = !isChecked,
            singleLine = true,
            textStyle = LocalTextStyle.current.merge(
                MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary),
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            cursorBrush = SolidColor(colors.accent),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(backgroundColor, RoundedCornerShape(10.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 16.dp),
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.numeric_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textSecondary.copy(alpha = 0.7f),
                        )
                    }
                    innerTextField()
                }
            },
        )
        if (isChecked) {
            NumericalResultRow(isCorrect = isCorrect, correctAnswerLabel = correctAnswerLabel)
        }
    }
}

@Composable
private fun NumericalResultRow(isCorrect: Boolean, correctAnswerLabel: String?) {
    val colors = appColors
    val accent = if (isCorrect) colors.correct else colors.incorrect
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier.size(20.dp).background(accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(if (isCorrect) R.drawable.ic_check else R.drawable.ic_close),
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier.size(13.dp),
            )
        }
        Text(
            text = when {
                isCorrect -> stringResource(R.string.numeric_correct)
                correctAnswerLabel != null -> stringResource(R.string.numeric_correct_with_value, correctAnswerLabel)
                else -> stringResource(R.string.numeric_incorrect)
            },
            style = MaterialTheme.typography.labelSmall,
            color = accent,
        )
    }
}

private fun String.filterNumeric(): String {
    val filtered = filterIndexed { index, char ->
        char.isDigit() || (char == '.' && !substring(0, index).contains('.')) ||
            ((char == '-' || char == '+') && index == 0)
    }
    return filtered.take(16)
}
