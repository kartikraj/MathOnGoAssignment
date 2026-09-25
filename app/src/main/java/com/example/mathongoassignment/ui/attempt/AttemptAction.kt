package com.example.mathongoassignment.ui.attempt

sealed interface AttemptAction {
    data class OptionClicked(val optionId: String) : AttemptAction
    data class NumericInputChanged(val input: String) : AttemptAction
    data object CheckAnswer : AttemptAction
    data object Previous : AttemptAction
    data object Next : AttemptAction
    data object Retry : AttemptAction
}
