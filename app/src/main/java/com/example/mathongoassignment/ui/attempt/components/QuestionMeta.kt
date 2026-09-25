package com.example.mathongoassignment.ui.attempt.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mathongoassignment.R
import com.example.mathongoassignment.ui.theme.appColors

@Composable
fun QuestionMeta(
    questionNumber: Int,
    totalQuestions: Int,
    hasVideoSolution: Boolean,
    sourceLabel: String?,
    modifier: Modifier = Modifier,
) {
    val colors = appColors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.question_counter, questionNumber, totalQuestions),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
            )
            if (hasVideoSolution) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                )
                Icon(
                    painter = painterResource(R.drawable.ic_video_solution),
                    contentDescription = stringResource(R.string.video_solution_available),
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        if (!sourceLabel.isNullOrBlank()) {
            Text(
                text = sourceLabel,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSource,
            )
        }
    }
}
