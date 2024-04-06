package com.alvaroliveira.wordaday.presentation

import com.alvaroliveira.wordaday.model.Word

sealed class UiState {
    data object Loading: UiState()
    data class Words(val words: List<Word>): UiState()
    data class Error(val error: Throwable): UiState()
}