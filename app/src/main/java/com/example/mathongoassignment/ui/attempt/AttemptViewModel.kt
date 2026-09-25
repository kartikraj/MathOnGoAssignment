package com.example.mathongoassignment.ui.attempt

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mathongoassignment.R
import com.example.mathongoassignment.data.AssetQuestionBankSource
import com.example.mathongoassignment.data.JsonQuestionBankRepository
import com.example.mathongoassignment.domain.answer.Answer
import com.example.mathongoassignment.domain.repository.QuestionBankException
import com.example.mathongoassignment.domain.repository.QuestionBankRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class AttemptViewModel(
    private val repository: QuestionBankRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AttemptUiState>(AttemptUiState.Loading)
    val uiState: StateFlow<AttemptUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onAction(action: AttemptAction) {
        setState(AttemptReducer.reduce(_uiState.value, action))
        if (action == AttemptAction.Retry) load()
    }

    private fun load() {
        setState(AttemptUiState.Loading)
        viewModelScope.launch {
            repository.loadQuestionBank()
                .onSuccess { bank ->
                    setState(
                        AttemptUiState.Ready(
                            bank = bank,
                            currentIndex = restoredIndex().coerceIn(0, bank.questions.lastIndex),
                            answers = restoredAnswers(),
                        ),
                    )
                }
                .onFailure { setState(AttemptUiState.Error(it.toUiMessage())) }
        }
    }

    private fun setState(newState: AttemptUiState) {
        val previous = _uiState.value
        if (newState == previous) return
        _uiState.value = newState
        if (newState is AttemptUiState.Ready) persist(previous as? AttemptUiState.Ready, newState)
    }

    private fun persist(previous: AttemptUiState.Ready?, current: AttemptUiState.Ready) {
        if (current.currentIndex != previous?.currentIndex) {
            savedStateHandle[KEY_INDEX] = current.currentIndex
        }
        if (current.answers != previous?.answers) {
            savedStateHandle[KEY_ANSWERS] = json.encodeToString(current.answers)
        }
    }

    private fun restoredIndex(): Int = savedStateHandle[KEY_INDEX] ?: 0

    private fun restoredAnswers(): Map<String, Answer> {
        val stored: String = savedStateHandle[KEY_ANSWERS] ?: return emptyMap()
        return runCatching { json.decodeFromString<Map<String, Answer>>(stored) }.getOrDefault(emptyMap())
    }

    companion object {
        private const val KEY_INDEX = "current_question_index"
        private const val KEY_ANSWERS = "answers"

        private val json = Json { ignoreUnknownKeys = true }

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                AttemptViewModel(
                    repository = JsonQuestionBankRepository(AssetQuestionBankSource(application)),
                    savedStateHandle = createSavedStateHandle(),
                )
            }
        }
    }
}

@StringRes
private fun Throwable.toUiMessage(): Int = when (this) {
    is QuestionBankException -> R.string.error_data_unreadable
    else -> R.string.error_unexpected
}
