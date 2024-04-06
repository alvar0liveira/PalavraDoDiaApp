package com.alvaroliveira.wordaday.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvaroliveira.wordaday.usecase.GetWordsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val TAG = "MainViewModel"

class MainViewModel(
    private val useCase: GetWordsUseCase
): ViewModel() {

    val state: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    var isOnline: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        isOnline
            .onEach {
                if (false){
                    state.emit(UiState.Error(Exception("No internet connection")))
                }
            }
    }

    fun getWords(){
        viewModelScope.launch {
            try {
                state.value = UiState.Words(useCase.getWords())
                Log.e(TAG, "getWords: ")
            } catch (e: Exception){
                state.value = UiState.Error(e)
            }
        }
    }
}