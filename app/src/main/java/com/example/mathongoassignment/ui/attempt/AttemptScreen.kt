package com.example.mathongoassignment.ui.attempt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mathongoassignment.R
import com.example.mathongoassignment.domain.answer.AnswerResult
import com.example.mathongoassignment.domain.model.QuestionType
import com.example.mathongoassignment.ui.attempt.components.AttemptBottomBar
import com.example.mathongoassignment.ui.attempt.components.NumericalAnswerInput
import com.example.mathongoassignment.ui.attempt.components.OptionCard
import com.example.mathongoassignment.ui.attempt.components.OptionState
import com.example.mathongoassignment.ui.attempt.components.QuestionMeta
import com.example.mathongoassignment.ui.richtext.RichContentView
import com.example.mathongoassignment.ui.theme.appColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttemptScreen(
    state: AttemptUiState,
    onAction: (AttemptAction) -> Unit,
    onBack: () -> Unit,
) {
    val colors = appColors
    val title = when (state) {
        is AttemptUiState.Ready -> listOf(state.examTitle, state.question.chapterTitle)
            .filter { it.isNotBlank() }
            .joinToString(" > ")
        else -> stringResource(R.string.attempt_title_fallback)
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onAppBar,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.action_back),
                            tint = colors.onAppBar,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.appBar,
                    titleContentColor = colors.onAppBar,
                ),
            )
        },
        bottomBar = {
            if (state is AttemptUiState.Ready) {
                AttemptBottomBar(
                    isCheckEnabled = state.isCheckEnabled,
                    isPreviousEnabled = state.canGoPrevious,
                    isNextEnabled = state.canGoNext,
                    onPrevious = { onAction(AttemptAction.Previous) },
                    onCheckAnswer = { onAction(AttemptAction.CheckAnswer) },
                    onNext = { onAction(AttemptAction.Next) },
                    // The action bar stays reachable when the numeric keyboard is open.
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.ime.union(WindowInsets.navigationBars),
                    ),
                )
            }
        },
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.background),
        ) {
            when (state) {
                AttemptUiState.Loading -> LoadingState()
                is AttemptUiState.Error -> ErrorState(message = stringResource(state.messageRes), onRetry = { onAction(AttemptAction.Retry) })
                is AttemptUiState.Ready -> ReadyState(state = state, onAction = onAction)
            }
        }
    }
}

@Composable
private fun ReadyState(state: AttemptUiState.Ready, onAction: (AttemptAction) -> Unit) {
    val colors = appColors
    val scrollState = rememberScrollState()

    // A new question always starts from the top of its content.
    LaunchedEffect(state.question.id) { scrollState.scrollTo(0) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp),
    ) {
        QuestionMeta(
            questionNumber = state.questionNumber,
            totalQuestions = state.totalQuestions,
            hasVideoSolution = state.question.hasVideoSolution,
            sourceLabel = state.question.sourceLabel,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp),
        )
        RichContentView(
            content = state.question.content,
            slotKey = "question",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            color = colors.textPrimary,
            placeholderHeight = 72.dp,
        )
        HorizontalDivider(color = colors.divider)

        val result = state.result
        when (state.question.type) {
            QuestionType.NUMERICAL -> NumericalAnswerInput(
                value = state.numericInput,
                isChecked = state.isChecked,
                isCorrect = result?.isCorrect == true,
                correctAnswerLabel = result?.correctAnswerLabel,
                onValueChange = { onAction(AttemptAction.NumericInputChanged(it)) },
                modifier = Modifier.padding(16.dp),
            )
            else -> OptionList(state = state, result = result, onAction = onAction)
        }
    }
}

@Composable
private fun OptionList(
    state: AttemptUiState.Ready,
    result: AnswerResult?,
    onAction: (AttemptAction) -> Unit,
) {
    val colors = appColors
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (state.question.type == QuestionType.MULTIPLE_CORRECT) {
            Text(
                text = stringResource(R.string.select_all_correct_options),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
            )
        }
        state.question.options.forEachIndexed { index, option ->

            OptionCard(
                label = optionLabel(index),
                content = option.content,
                slotKey = "option-$index",
                state = optionState(option.id, state.selectedOptionIds, result),
                enabled = !state.isChecked,
                onClick = { onAction(AttemptAction.OptionClicked(option.id)) },
            )
        }
    }
}

private fun optionState(optionId: String, selectedOptionIds: Set<String>, result: AnswerResult?): OptionState {
    val isSelected = optionId in selectedOptionIds
    if (result == null) return if (isSelected) OptionState.Selected else OptionState.Default
    return when {
        optionId in result.correctOptionIds -> OptionState.Correct
        isSelected -> OptionState.Incorrect
        else -> OptionState.Default
    }
}

private fun optionLabel(index: Int): String =
    if (index < 26) ('A' + index).toString() else (index + 1).toString()

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = appColors.accent)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    val colors = appColors
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.error_title),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            Box(Modifier.height(8.dp))
            TextButton(onClick = onRetry) {
                Text(
                    text = stringResource(R.string.action_try_again),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.accent,
                )
            }
        }
    }
}
